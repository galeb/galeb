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

import io.galeb.core.entity.Rule;
import io.galeb.core.entity.RuleOrdered;
import io.galeb.core.entity.VirtualhostGroup;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import io.galeb.oldapi.entities.v1.RuleOrder;
import io.galeb.oldapi.entities.v1.VirtualHost;
import io.galeb.oldapi.services.components.ConverterV2;
import io.galeb.oldapi.services.http.Response;
import javafx.collections.transformation.SortedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.util.Pair;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VirtualHostService extends AbstractConverterService<VirtualHost> {

    private static final Logger LOGGER = LogManager.getLogger(VirtualHostService.class);

    private static final String[] ADD_REL = {"ruleDefault", "rules", "project", "environment"};
    private static final String[] DEL_REL = {"rulesordered", "virtualhostgroup", "virtualhosts"};

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

    @Override
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> get(Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass, Map<String, String> queryMap) {
        int size = getSizeRequest(queryMap);
        int page = getPageRequest(queryMap);
        String url = fullUrlWithSizeAndPage(size, page);
        try {
            Response response = httpClientService.getResponse(url);
            ConverterV2.V2JsonHalData v2JsonHalData = converterV2.toV2JsonHal(response, v2entityClass);
            Set<Resource<? extends AbstractEntity>> v1Entities = v2JsonHalData.getV2entities().stream()
                    .map(v2 -> {
                        try {
                            Set<Link> links = new HashSet<>(v2.getLinks());
                            v2LinksToV1Links(links, v2.getContent().getId());
                            VirtualHost virtualhostV1 = (VirtualHost) converterV1.v2ToV1(v2.getContent(), v2entityClass, VirtualHost.class);
                            String rulesOrderedUrl = v2.getLink("rulesordered").getHref();
                            String virtualhostsUrl = v2.getLink("virtualhosts").getHref();
                            ConverterV2.V2JsonHalData rulesOrderedJsonHalData = converterV2.toV2JsonHal(httpClientService.getResponse(rulesOrderedUrl), RuleOrdered.class);
                            ConverterV2.V2JsonHalData virtualhostsJsonHalData = converterV2.toV2JsonHal(httpClientService.getResponse(virtualhostsUrl), io.galeb.core.entity.VirtualHost.class);
                            Set<RuleOrder> rulesOrdered = rulesOrderedJsonHalData.getV2entities().stream()
                                    .map(ro -> {
                                        try {
                                            String ruleUrl = ro.getLink("rule").getHref();
                                            Rule rule = (Rule) converterV2.toV2JsonHal(httpClientService.getResponse(ruleUrl), Rule.class)
                                                    .getV2entities()
                                                    .stream().findAny()
                                                    .orElse(new Resource<>(new Rule(), Collections.emptyList()))
                                                    .getContent();
                                            long ruleId = rule.getId();
                                            int order = ((RuleOrdered) ro.getContent()).getOrder();
                                            return new RuleOrder(ruleId, order);
                                        } catch (Exception ignored) {
                                        }
                                        return null;
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toSet());
                            LinkedList<String> virtualhostNames = virtualhostsJsonHalData.getV2entities().stream()
                                    .map(Resource::getContent)
                                    .sorted(Comparator.comparingLong(io.galeb.core.entity.AbstractEntity::getId))
                                    .map(e -> ((io.galeb.core.entity.VirtualHost) e).getName())
                                    .collect(Collectors.toCollection(LinkedList::new));
                            virtualhostV1.setRulesOrdered(rulesOrdered);
                            virtualhostV1.setName(virtualhostNames.getFirst());
                            virtualhostV1.setAliases(new HashSet<>(virtualhostNames));
                            return new Resource<>(virtualhostV1, links);
                        } catch (Exception e) {
                            LOGGER.error(e);
                            return null;
                        }
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
    public ResponseEntity<Resource<? extends AbstractEntity>> getWithId(String id, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        String url = resourceUrlBase + "/" + id;
        try {
            Response response = httpClientService.getResponse(url);
            return processResponse(response, Long.parseLong(id), HttpMethod.GET, v2entityClass);
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }
}
