/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.oldapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.galeb.core.entity.Rule;
import io.galeb.core.entity.RuleOrdered;
import io.galeb.core.entity.VirtualhostGroup;
import io.galeb.oldapi.entities.v1.*;
import io.galeb.oldapi.exceptions.BadRequestException;
import io.galeb.oldapi.services.components.ConverterV2;
import io.galeb.oldapi.services.http.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class VirtualHostService extends AbstractConverterService<VirtualHost> {

    private static final Logger LOGGER = LogManager.getLogger(VirtualHostService.class);

    private static final String[] ADD_REL = {"ruleDefault", "rules", "project", "environment"};
    private static final String[] DEL_REL = {"rulesordered", "virtualhostgroup", "environments", "virtualhosts"};

    @Override
    String[] addRel() {
        return ADD_REL;
    }

    @Override
    String[] delRel() {
        return DEL_REL;
    }

    @Override
    String getResourceName() {
        return VirtualhostGroup.class.getSimpleName().toLowerCase();
    }

    private List<io.galeb.core.entity.VirtualHost> makeAliasesV2(VirtualHost virtualHost, io.galeb.core.entity.Project projectV2, io.galeb.core.entity.Environment environmentV2) {
        Environment environment = virtualHost.getEnvironment();
        Project project = virtualHost.getProject();
        return virtualHost.getAliases().stream().map(alias -> {
            io.galeb.core.entity.VirtualHost v2 = new io.galeb.core.entity.VirtualHost();
            v2.setName(alias);

            long environmentId = Long.parseLong(environment.getName().replaceAll("^.*/", ""));
            long projectId = Long.parseLong(project.getName().replaceAll("^.*/", ""));

            environmentV2.setId(environmentId);
            projectV2.setId(projectId);
            v2.setProject(projectV2);
            v2.setEnvironments(Collections.singleton(environmentV2));
            return v2;
        }).collect(Collectors.toList());
    }

    private JsonNode rebuildVirtualhostV2Json(io.galeb.core.entity.VirtualHost virtualHost) {
        ArrayNode arrayOfEnvironments = new ArrayNode(JsonNodeFactory.instance);
        JsonNode jsonNode = convertFromJsonObjToJsonNode(virtualHost);
        ((ObjectNode) jsonNode).remove("virtualhostgroup");
        VirtualhostGroup virtualhostgroup = virtualHost.getVirtualhostgroup();
        if (virtualhostgroup != null) {
            ((ObjectNode) jsonNode).put("virtualhostgroup", "http://localhost/virtualhostgroup/" + virtualhostgroup.getId());
        }
        ((ObjectNode) jsonNode).put("project", "http://localhost/project/" + virtualHost.getProject().getId());
        for (io.galeb.core.entity.Environment e: virtualHost.getEnvironments()) {
            arrayOfEnvironments.add("http://localhost/environment/" + e.getId());
        }
        ((ObjectNode) jsonNode).replace("environments", arrayOfEnvironments);
        return jsonNode;
    }

    private Resource<VirtualHost> convertVirtualhostToV1(Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass,
                                                         Resource<? extends io.galeb.core.entity.AbstractEntity> v2, String envname) {
        try {
            Set<Link> links = new HashSet<>(v2.getLinks());
            v2LinksToV1Links(links, v2.getContent().getId());
            VirtualHost virtualhostV1 = (VirtualHost) converterV1.v2ToV1(v2.getContent(), v2entityClass, VirtualHost.class);
            Long vhgid = virtualhostV1.getId();
            if (envname == null || envname.isEmpty()) {
                List<io.galeb.core.entity.Environment> environments = extractEnvironmentsFromVirtuahostGroupId(vhgid);
                if (environments == null || environments.size() > 1) {
                    throw new BadRequestException("Virtualhost ID " + vhgid + ": param 'environment' is mandatory. Ignoring.");
                }
                envname = environments.stream().map(io.galeb.core.entity.Environment::getName).findAny().orElse("UNDEF");
            }
            Set<RuleOrder> rulesOrdered = extractRulesOrderedByEnvironment(envname, vhgid);
            LinkedList<? extends io.galeb.core.entity.AbstractEntity> virtualhosts = extractVirtualhostsByEnvironment(envname, vhgid);
            LinkedList<String> virtualhostNames = virtualhosts.stream().map(v -> ((io.galeb.core.entity.VirtualHost)v).getName()).collect(Collectors.toCollection(LinkedList::new));

            if (virtualhostNames == null || virtualhostNames.isEmpty()) {
                throw new IllegalArgumentException("VirtualhostNames is empty");
            }
            virtualhostV1.setRulesOrdered(rulesOrdered);
            virtualhostV1.setName(virtualhostNames.getFirst());
            virtualhostV1.setAliases(new HashSet<>(virtualhostNames));
            return new Resource<>(virtualhostV1, links);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    private List<io.galeb.core.entity.Environment> extractEnvironmentsFromVirtuahostGroupId(Long vhgid) throws InterruptedException, ExecutionException {
        String urlEnvsFromVirtualhostGroupId = apiUrl + "/custom-search/environment/findAllByVirtualhostgroupId?vhgid=" + vhgid;
        Response allEnvsResponse = httpClientService.getResponse(urlEnvsFromVirtualhostGroupId);
        ConverterV2.V2JsonHalData envJsonHalData = converterV2.toV2JsonHal(allEnvsResponse, io.galeb.core.entity.Environment.class);
        return envJsonHalData.getV2entities().stream()
                        .map(r -> (io.galeb.core.entity.Environment)r.getContent())
                        .collect(Collectors.toList());
    }

    private LinkedList<? extends io.galeb.core.entity.AbstractEntity> extractVirtualhostsByEnvironment(String envname, Long vhgid) throws InterruptedException, ExecutionException {
        String virtualhostsUrl = apiUrl + "/virtualhost/search/findByVirtualhostgroup_IdAndEnvironments_Name?vhgid=" + vhgid + "&envname=" + envname;
        ConverterV2.V2JsonHalData virtualhostsJsonHalData = converterV2.toV2JsonHal(httpClientService.getResponse(virtualhostsUrl), io.galeb.core.entity.VirtualHost.class);
        return extractVirtualhost(virtualhostsJsonHalData);
    }

    private Set<RuleOrder> extractRulesOrderedByEnvironment(String envname, Long vhgid) throws InterruptedException, ExecutionException {
        String rulesOrderedUrl = apiUrl + "/ruleordered/search/findByVirtualhostgroup_IdAndEnvironment_Name?vhgid=" + vhgid + "&envname=" + envname;
        ConverterV2.V2JsonHalData rulesOrderedJsonHalData = converterV2.toV2JsonHal(httpClientService.getResponse(rulesOrderedUrl), RuleOrdered.class);
        return extractRuleOrders(rulesOrderedJsonHalData);
    }

    private LinkedList<? extends io.galeb.core.entity.AbstractEntity> extractVirtualhost(ConverterV2.V2JsonHalData virtualhostsJsonHalData) {
        return virtualhostsJsonHalData.getV2entities().stream()
                .map(Resource::getContent)
                .sorted(Comparator.comparingLong(io.galeb.core.entity.AbstractEntity::getId))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private Set<RuleOrder> extractRuleOrders(ConverterV2.V2JsonHalData rulesOrderedJsonHalData) {
        return rulesOrderedJsonHalData.getV2entities().stream()
                .map(ro -> {
                    try {
                        String ruleUrl = ro.getLink("rule").getHref();
                        Rule rule = extractRule(ruleUrl);
                        long ruleId = rule.getId();
                        int order = ((RuleOrdered) ro.getContent()).getOrder();
                        return new RuleOrder(ruleId, order);
                    } catch (Exception ignored) { }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private VirtualhostGroup extractVirtualhostGroup(String virtualhostGroupUrl) throws InterruptedException, ExecutionException {
        return (VirtualhostGroup) converterV2.toV2JsonHal(httpClientService.getResponse(virtualhostGroupUrl), VirtualhostGroup.class)
                .getV2entities()
                .stream().findAny()
                .orElse(new Resource<>(new VirtualhostGroup(), Collections.emptyList()))
                .getContent();
    }

    private Rule extractRule(String ruleUrl) throws InterruptedException, ExecutionException {
        return (Rule) converterV2.toV2JsonHal(httpClientService.getResponse(ruleUrl), Rule.class)
                .getV2entities()
                .stream().findAny()
                .orElse(new Resource<>(new Rule(), Collections.emptyList()))
                .getContent();
    }

    private ResponseEntity<Resource<? extends AbstractEntity>> doMethod(String body, HttpMethod method, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        VirtualHost virtualHost = convertFromJsonStringToV1(body);

        if (virtualHost != null) {
            Environment environment = virtualHost.getEnvironment();
            if (environment == null) {
                throw new BadRequestException("Environment is mandatory");
            }
            Project project = virtualHost.getProject();
            if (project == null) {
                throw new BadRequestException("Project is mandatory");
            }
            io.galeb.core.entity.Environment environmentV2 = new io.galeb.core.entity.Environment();
            environmentV2.setId(environment.getId());

            io.galeb.core.entity.Project projectV2 = new io.galeb.core.entity.Project();
            projectV2.setId(project.getId());

            io.galeb.core.entity.VirtualHost virtualhostV2 = new io.galeb.core.entity.VirtualHost();
            virtualhostV2.setName(virtualHost.getName());
            virtualhostV2.setProject(projectV2);
            virtualhostV2.setEnvironments(Collections.singleton(environmentV2));

            try {
                JsonNode jsonNode = rebuildVirtualhostV2Json(virtualhostV2);
                Response response = sendMethodRequest(method, jsonNode);
                ConverterV2.V2JsonHalData virtualhostJsonHal = converterV2.toV2JsonHal(response, io.galeb.core.entity.VirtualHost.class);
                String virtualhostgroupUrl = virtualhostJsonHal.getLinks().get("virtualhostgroup");
                VirtualhostGroup virtualhostGroup = extractVirtualhostGroup(virtualhostgroupUrl);
                for (io.galeb.core.entity.VirtualHost v2Alias: makeAliasesV2(virtualHost, projectV2, environmentV2)) {
                    v2Alias.setVirtualhostgroup(virtualhostGroup);
                    JsonNode jsonNodeAlias = rebuildVirtualhostV2Json(v2Alias);
                    sendMethodRequest(method, jsonNodeAlias);
                }
                return processResponse(response, -1, method, v2entityClass);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    private Response sendMethodRequest(HttpMethod method, JsonNode jsonNode) throws InterruptedException, ExecutionException {
        String virtualhostResourcePath = VirtualHost.class.getSimpleName().toLowerCase();
        if (method == HttpMethod.PUT) {
            Response responseV2 = httpClientService.getResponse(virtualhostResourcePath + "/search/findByName?name=" + jsonNode.get("name"));
            ConverterV2.V2JsonHalData v2JsonHalData = converterV2.toV2JsonHal(responseV2, io.galeb.core.entity.VirtualHost.class);
            List<Resource<? extends io.galeb.core.entity.AbstractEntity>> v2entities = v2JsonHalData.getV2entities();
            if (v2entities != null && !v2entities.isEmpty()) {
                long vh2Id = v2entities.stream().findAny().get().getContent().getId();
                return httpClientService.put(apiUrl + "/" + virtualhostResourcePath + "/" + vh2Id, jsonNode.toString());
            }
        }
        return httpClientService.post(apiUrl + "/" + virtualhostResourcePath, jsonNode.toString());
    }

    private ResponseEntity<?> getInternal(String id, Map<String, String> queryMap, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass, String url) throws InterruptedException, ExecutionException {
        Response response = httpClientService.getResponse(url);
        ConverterV2.V2JsonHalData v2JsonHalData = converterV2.toV2JsonHal(response, v2entityClass);
        String envname = queryMap.get("environment");
        if (id == null || id.isEmpty()) {
            Set<Resource<? extends AbstractEntity>> v1Entities = v2JsonHalData.getV2entities().stream()
                    .map(v2 -> {
                        try {
                            return convertVirtualhostToV1(v2entityClass, v2, envname);
                        } catch (IllegalArgumentException e) {
                            LOGGER.error(e);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            final PagedResources<Resource<? extends AbstractEntity>> pagedResources = buildPagedResources(v1Entities, queryMap);
            return ResponseEntity.ok(pagedResources);
        } else {
            Optional<Resource<? extends io.galeb.core.entity.AbstractEntity>> v2Resource = v2JsonHalData.getV2entities().stream().findAny();
            if (v2Resource.isPresent()) {
                Resource<VirtualHost> v1 = convertVirtualhostToV1(v2entityClass, v2Resource.get(), envname);
                return processResource(Long.parseLong(id), HttpMethod.GET, v1);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> get(Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass, Map<String, String> queryMap) {
        String url = fullUrlWithSizeAndPage(queryMap);
        try {
            return (ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>>) getInternal(null, queryMap, v2entityClass, url);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resource<? extends AbstractEntity>> getWithId(String id, Map<String, String> queryMap, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        String url = resourceUrlBase + "/" + id;
        try {
            return (ResponseEntity<Resource<? extends AbstractEntity>>) getInternal(id, queryMap, v2entityClass, url);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<? extends AbstractEntity>> post(String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        return doMethod(body, HttpMethod.POST, v2entityClass);
    }

    @Override
    public ResponseEntity<Resource<? extends AbstractEntity>> putWithId(String id, String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        String bodyWithId = ((ObjectNode)convertFromJsonStrToJsonNode(body)).put("id", Long.parseLong(id)).toString();
        return doMethod(bodyWithId, HttpMethod.PUT, v2entityClass);
    }

    @Override
    public ResponseEntity<Void> patchWithId(String id, String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        JsonNode v1FE = convertFromJsonStrToJsonNode(body);
        ResponseEntity<Resource<? extends AbstractEntity>> responseV1BE = getWithId(id, Collections.emptyMap(), v2entityClass);
        VirtualHost virtualHost = (VirtualHost) responseV1BE.getBody().getContent();
        if (virtualHost != null) {
            JsonNode v1BE = convertFromJsonObjToJsonNode(virtualHost);
            if (v1BE != null) {
                v1FE.fields().forEachRemaining(e -> ((ObjectNode) v1BE).replace(e.getKey(), e.getValue()));
                putWithId(id, v1BE.toString(), v2entityClass);
                return ResponseEntity.noContent().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
