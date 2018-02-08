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

    private JsonNode rebuildVirtualhostV2Json(io.galeb.core.entity.VirtualHost virtualHost) {
        ArrayNode arrayOfEnvironments = new ArrayNode(JsonNodeFactory.instance);
        JsonNode jsonNodeAlias = convertFromJsonStrToJsonNode(virtualHost);
        ((ObjectNode) jsonNodeAlias).put("virtualhostgroup", "http://localhost/virtualhostgroup/" + virtualHost.getVirtualhostgroup().getId());
        ((ObjectNode) jsonNodeAlias).put("project", "http://localhost/project/" + virtualHost.getProject().getId());
        for (io.galeb.core.entity.Environment e: virtualHost.getEnvironments()) {
            arrayOfEnvironments.add("http://localhost/environment/" + e.getId());
        }
        ((ObjectNode) jsonNodeAlias).replace("environments", arrayOfEnvironments);
        return jsonNodeAlias;
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

    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> get(Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass, Map<String, String> queryMap) {
        int size = getSizeRequest(queryMap);
        int page = getPageRequest(queryMap);
        String url = fullUrlWithSizeAndPage(size, page);
        try {
            Response response = httpClientService.getResponse(url);
            ConverterV2.V2JsonHalData v2JsonHalData = converterV2.toV2JsonHal(response, v2entityClass);
            String envname = queryMap.get("environment");
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

            final PagedResources<Resource<? extends AbstractEntity>> pagedResources = buildPagedResources(size, page, v1Entities);
            return ResponseEntity.ok(pagedResources);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<? extends AbstractEntity>> getWithId(String id, Map<String, String> queryMap, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        String url = resourceUrlBase + "/" + id;
        try {
            Response response = httpClientService.getResponse(url);
            ConverterV2.V2JsonHalData v2JsonHalData = converterV2.toV2JsonHal(response, v2entityClass);
            Optional<Resource<? extends io.galeb.core.entity.AbstractEntity>> v2Resource = v2JsonHalData.getV2entities().stream().findAny();
            if (v2Resource.isPresent()) {
                String envname = queryMap.get("environment");
                Resource<VirtualHost> v1 = convertVirtualhostToV1(v2entityClass, v2Resource.get(), envname);
                return processResource(Long.parseLong(id), HttpMethod.GET, v1);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<? extends AbstractEntity>> post(String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        VirtualHost virtualHost = convertFromJsonStringToV1(body);

        if (virtualHost != null) {
            Environment environment = virtualHost.getEnvironment();
            Project project = virtualHost.getProject();

            io.galeb.core.entity.Project projectV2 = new io.galeb.core.entity.Project();
            io.galeb.core.entity.Environment environmentV2 = new io.galeb.core.entity.Environment();

            List<io.galeb.core.entity.VirtualHost> aliases = virtualHost.getAliases().stream().map(alias -> {
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

            io.galeb.core.entity.VirtualHost virtualhostV2 = new io.galeb.core.entity.VirtualHost();
            virtualhostV2.setName(virtualHost.getName());
            virtualhostV2.setEnvironments(Collections.singleton(environmentV2));
            virtualhostV2.setProject(projectV2);

            try {

                JsonNode jsonNode = rebuildVirtualhostV2Json(virtualhostV2);
                String virtualhostResourcePath = VirtualHost.class.getSimpleName().toLowerCase();
                Response response = httpClientService.post(apiUrl + "/" + virtualhostResourcePath, jsonNode.toString());
                ConverterV2.V2JsonHalData virtualhostJsonHal = converterV2.toV2JsonHal(response, io.galeb.core.entity.VirtualHost.class);
                String virtualhostgroupUrl = virtualhostJsonHal.getLinks().get("virtualhostgroup");
                VirtualhostGroup virtualhostGroup = extractVirtualhostGroup(virtualhostgroupUrl);
                io.galeb.core.entity.VirtualHost responseV2 = (io.galeb.core.entity.VirtualHost) converterV2.convertJsonStringToV2(response.getResponseBody(), io.galeb.core.entity.VirtualHost.class);

                for (io.galeb.core.entity.VirtualHost v2Alias: aliases) {
                    v2Alias.setVirtualhostgroup(virtualhostGroup);
                    JsonNode jsonNodeAlias = rebuildVirtualhostV2Json(v2Alias);
                    httpClientService.post(apiUrl + "/" + virtualhostResourcePath, jsonNodeAlias.toString());
                }

                return processResponse(response, -1, HttpMethod.POST, v2entityClass);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
