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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FarmService extends AbstractConverterService<Farm> {

    private static final Logger LOGGER = LogManager.getLogger(FarmService.class);

    private final EnvironmentService environmentService;
    private final LinkProcessor linkProcessor;

    @Autowired
    public FarmService(LinkProcessor linkProcessor,
                       HttpClientService httpClientService,
                       EnvironmentService environmentService,
                       @Value("${api.url}") String apiUrl) {
        super(linkProcessor, httpClientService);
        this.environmentService = environmentService;
        this.resourceUrlBase = apiUrl + "/" + getResourceName();
        this.linkProcessor = linkProcessor;
    }

    @Override
    void fixV1Links(Set<Link> links, Long id) {
        linkProcessor.remove(links, "farms")
                     .remove(links, "pools")
                     .remove(links, "targets")
                     .remove(links, "environment")
                     .remove(links, "virtualhosts")
                     .add(links, "/" + getResourceName() + "/" + id + "/environment", "environment")
                     .add(links, "/" + getResourceName() + "/" + id + "/provider", "provider");
    }

    @Override
    public ResponseEntity<PagedResources<Resource<Farm>>> get(Integer size, Integer page, Class<? extends AbstractEntity> v2entityClass) {
        size = size == null ? 99999 : size;
        page = page == null ? 0 : page;
        Set<Resource<Farm>> resources = environmentService.get(size, page, Environment.class).getBody().getContent().stream().map(r -> {
            Farm farm = new Farm();
            io.galeb.oldapi.entities.v1.Environment environment = r.getContent();
            farm.setName(environment.getName());
            farm.setId(environment.getId());
            farm.setCreatedAt(environment.getCreatedAt());
            farm.setCreatedBy(environment.getCreatedBy());
            farm.setLastModifiedAt(environment.getLastModifiedAt());
            farm.setLastModifiedBy(environment.getLastModifiedBy());
            farm.setStatus(environment.getStatus());
            Set<Link> links = new HashSet<>(r.getLinks());
            fixV1Links(links, farm.getId());
            List<Link> newLinks = links.stream().map(l -> {
                String newHref = l.getHref().replaceAll("/environment/", "/farm/");
                return new Link(newHref, l.getRel());
            }).collect(Collectors.toList());
            return new Resource<>(farm, newLinks);
        }).collect(Collectors.toSet());
        PagedResources<Resource<Farm>> pagedResources = buildPagedResources(size, page, resources);
        return ResponseEntity.ok(pagedResources);
    }
}
