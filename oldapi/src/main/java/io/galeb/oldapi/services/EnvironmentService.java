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

import io.galeb.oldapi.entities.v1.Environment;
import io.galeb.oldapi.services.http.HttpClientService;
import io.galeb.oldapi.services.http.Response;
import io.galeb.oldapi.services.utils.LinkProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class EnvironmentService extends AbstractConverterService<Environment> {

    private static final Logger LOGGER = LogManager.getLogger(EnvironmentService.class);

    private final String resourceUrlBase;
    private final HttpClientService httpClientService;
    private final LinkProcessor linkProcessor;

    @Autowired
    public EnvironmentService(HttpClientService httpClientService, LinkProcessor linkProcessor, @Value("${api.url}") String apiUrl) {
        super();
        this.httpClientService = httpClientService;
        this.linkProcessor = linkProcessor;
        this.resourceUrlBase = apiUrl + "/" + getResourceName();
    }

    @Override
    Set<Resource<Environment>> convertResources(ArrayList<LinkedHashMap> v2s) {
        return v2s.stream().
                map(resource -> {
                    try {
                        Environment environment = convertResource(resource, io.galeb.core.entity.Environment.class);
                        Set<Link> links = linkProcessor.extractLinks(resource, getResourceName());
                        Long id = linkProcessor.extractId(links);
                        fixV1Links(links, id);
                        environment.setId(id);
                        return new Resource<>(environment, links);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    protected void fixV1Links(Set<Link> links, Long id) {
        linkProcessor.add(links,"/" + getResourceName() + "/" + id + "/farms", "farms")
                     .add(links,"/" + getResourceName() + "/" + id + "/targets", "targets")
                     .remove(links, "rulesordered");
    }

    @Override
    public ResponseEntity<PagedResources<Resource<Environment>>> get(Integer size, Integer page) {
        String url = resourceUrlBase + "?size=" + (size != null ? size : 99999) + "&page=" + (page != null ? page : 0);
        try {
            Response response = httpClientService.getResponse(url);
            final Set<Resource<Environment>> resources = convertResources(extractArrayOfMapsFromBody(getResourceName(), response));
            final PagedResources<Resource<Environment>> pagedResources = buildPagedResources(size, page, resources, linkProcessor);
            return ResponseEntity.ok(pagedResources);
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<Environment>> getWithId(String id) {
        String url = resourceUrlBase + "/" + id;
        try {
            Response response = httpClientService.getResponse(url);
            return buildResource(response, Long.parseLong(id), HttpMethod.GET, linkProcessor, io.galeb.core.entity.Environment.class);
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<Environment>> post(String body) {
        Environment environment = stringToEntityV1(body);
        if (environment != null) {
            try {
                Response response = httpClientService.post(resourceUrlBase, body);
                return buildResource(response, -1, HttpMethod.POST, linkProcessor, io.galeb.core.entity.Environment.class);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<Environment>> putWithId(String id, String body) {
        Environment environment = stringToEntityV1(body);
        if (environment != null) {
            try {
                Response response = httpClientService.put(resourceUrlBase + "/" + id, body);
                return buildResource(response, Long.parseLong(id), HttpMethod.PUT, linkProcessor, io.galeb.core.entity.Environment.class);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
