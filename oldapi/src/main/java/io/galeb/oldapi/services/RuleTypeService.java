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

import io.galeb.core.entity.AbstractEntity;
import io.galeb.oldapi.entities.v1.RuleType;
import io.galeb.oldapi.services.http.HttpClientService;
import io.galeb.oldapi.services.utils.LinkProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RuleTypeService extends AbstractConverterService<RuleType> {

    private static final Logger LOGGER = LogManager.getLogger(RuleTypeService.class);

    private final Resource<RuleType> resource;
    private final LinkProcessor linkProcessor;

    @Autowired
    public RuleTypeService(LinkProcessor linkProcessor, HttpClientService httpClientService, @Value("${api.url}") String apiUrl) {
        super(linkProcessor, httpClientService);
        this.linkProcessor = linkProcessor;
        final RuleType ruleTypeUrlPath = new RuleType("UrlPath");
        ruleTypeUrlPath.setId(1L);
        final List<Link> links = Collections.singletonList(new Link("/" + getResourceName() + "/1", "self"));
        this.resource = new Resource<>(ruleTypeUrlPath, links);
        this.resourceUrlBase = apiUrl + "/" + getResourceName();
    }

    @Override
    public ResponseEntity<PagedResources<Resource<RuleType>>> getSearch(String findType, Map<String, String> queryMap) {
        if ("findByName".equals(findType) && !"UrlPath".equals(queryMap.get("name"))) return ResponseEntity.notFound().build();
        if ("findByNameContaining".equals(findType) && !"UrlPath".equals(queryMap.get("name"))) return ResponseEntity.notFound().build();
        return get(0, 0, null);
    }

    @Override
    public ResponseEntity<PagedResources<Resource<RuleType>>> get(Integer size, Integer page, Class<? extends AbstractEntity> v2entityClass) {
        size = size != null ? size : 9999;
        page = page != null ? page : 0;
        Set<Resource<RuleType>> v1Resources = Collections.singleton(resource);
        final PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(1, 0, 1, 1);
        final PagedResources<Resource<RuleType>> pagedResources = new PagedResources<>(v1Resources, metadata, linkProcessor.pagedLinks(getResourceName(), size, page));
        return ResponseEntity.ok(pagedResources);
    }

    public ResponseEntity<Resource<RuleType>> getWithId(String param, Class<? extends AbstractEntity> v2entityClass) {
        return ResponseEntity.ok(resource);
    }

}
