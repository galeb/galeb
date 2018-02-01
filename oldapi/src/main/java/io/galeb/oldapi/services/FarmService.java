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
import io.galeb.core.entity.Environment;
import io.galeb.oldapi.entities.v1.Farm;
import io.galeb.oldapi.services.components.LinkProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FarmService extends AbstractConverterService<Farm> {

    private static final Logger LOGGER = LogManager.getLogger(FarmService.class);

    private final EnvironmentService environmentService;
    private final LinkProcessor linkProcessor;

    @Autowired
    public FarmService(LinkProcessor linkProcessor, EnvironmentService environmentService) {
        super();
        this.environmentService = environmentService;
        this.linkProcessor = linkProcessor;
    }

    private Farm convertEnvToFarm(io.galeb.oldapi.entities.v1.Environment environment) {
        Farm farm = new Farm();
        farm.setName(environment.getName());
        farm.setId(environment.getId());
        farm.setCreatedAt(environment.getCreatedAt());
        farm.setCreatedBy(environment.getCreatedBy());
        farm.setLastModifiedAt(environment.getLastModifiedAt());
        farm.setLastModifiedBy(environment.getLastModifiedBy());
        farm.setStatus(environment.getStatus());
        return farm;
    }

    @Override
    void convertFromV2LinksToV1Links(Set<Link> links, Long id) {
        linkProcessor.add(links, "/" + getResourceName() + "/" + id + "/environment", "environment")
                     .add(links, "/" + getResourceName() + "/" + id + "/provider", "provider");
    }

    @Override
    public ResponseEntity<PagedResources<Resource<Farm>>> get(Class<? extends AbstractEntity> v2entityClass, Map<String, String> queryMap) {
        int size = getSizeRequest(queryMap);
        int page = getPageRequest(queryMap);
        Set<Resource<Farm>> resources = environmentService.get(Environment.class, queryMap).getBody().getContent().stream().map(r -> {
            io.galeb.oldapi.entities.v1.Environment environment = r.getContent();
            Farm farm = convertEnvToFarm(environment);
            Set<Link> links = new HashSet<>();
            convertFromV2LinksToV1Links(links, farm.getId());
            return new Resource<>(farm, links);
        }).collect(Collectors.toSet());
        PagedResources<Resource<Farm>> pagedResources = buildPagedResources(size, page, resources);
        return ResponseEntity.ok(pagedResources);
    }

    @Override
    public ResponseEntity<Resource<Farm>> getWithId(String id, Class<? extends AbstractEntity> v2entityClass) {
        ResponseEntity<Resource<io.galeb.oldapi.entities.v1.Environment>> environmentResourceResponse = environmentService.getWithId(id, Environment.class);
        final Resource<io.galeb.oldapi.entities.v1.Environment> environmentResource;
        if (environmentResourceResponse == null || (environmentResource = environmentResourceResponse.getBody()) == null ) {
            return ResponseEntity.notFound().build();
        }
        io.galeb.oldapi.entities.v1.Environment environment = environmentResource.getContent();
        Set<Link> links = new HashSet<>();
        convertFromV2LinksToV1Links(links, Long.parseLong(id));
        Farm farm = convertEnvToFarm(environment);
        return ResponseEntity.ok(new Resource<>(farm, links));
    }

    @Override
    public ResponseEntity<Resource<Farm>> post(String body, Class<? extends AbstractEntity> v2entityClass) {
        return ResponseEntity.created(URI.create("/farm")).build();
    }

    @Override
    public ResponseEntity<Resource<Farm>> putWithId(String id, String body, Class<? extends AbstractEntity> v2entityClass) {
        return ResponseEntity.noContent().build();
    }
}
