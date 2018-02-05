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

import io.galeb.oldapi.entities.v1.AbstractEntity;
import io.galeb.oldapi.entities.v1.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ProviderService extends AbstractConverterService<Provider> {

    private static final Logger LOGGER = LogManager.getLogger(ProviderService.class);

    private final Resource<Provider> resource;

    @Autowired
    public ProviderService() {
        super();
        final Provider providerInstance = new Provider("Default");
        providerInstance.setId(1L);
        final List<Link> links = Collections.singletonList(new Link("/" + getResourceName() + "/1", "self"));
        this.resource = new Resource<>(providerInstance, links);
    }

    @Override
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> getSearch(String findType, Map<String, String> queryMap) {
        if ("findByName".equals(findType) && !"Default".equals(queryMap.get("name"))) return ResponseEntity.notFound().build();
        if ("findByNameContaining".equals(findType) && !"Default".equals(queryMap.get("name"))) return ResponseEntity.notFound().build();
        return get(null, queryMap);
    }

    @Override
    public ResponseEntity<PagedResources<Resource<? extends AbstractEntity>>> get(Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass, Map<String, String> queryMap) {
        int size = getSizeRequest(queryMap);
        int page = getPageRequest(queryMap);
        Set<Resource<? extends AbstractEntity>> v1Resources = Collections.singleton(resource);
        final PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(1, 0, 1, 1);
        final PagedResources<Resource<? extends AbstractEntity>> pagedResources = new PagedResources<>(v1Resources, metadata, pagedLinks(getResourceName(), size, page));
        return ResponseEntity.ok(pagedResources);
    }

    public ResponseEntity<Resource<? extends AbstractEntity>> getWithId(String param, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        return ResponseEntity.ok(resource);
    }

}
