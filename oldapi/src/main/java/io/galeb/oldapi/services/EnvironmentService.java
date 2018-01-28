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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class EnvironmentService extends AbstractConverterService<Environment> {

    private static final Logger LOGGER = LogManager.getLogger(EnvironmentService.class);

    private final String resourceName = Environment.class.getSimpleName().toLowerCase();
    private final String resourceUrlBase;
    private final HttpClientService httpClientService;
    private final LinkProcessor linkProcessor;

    @Autowired
    public EnvironmentService(HttpClientService httpClientService, LinkProcessor linkProcessor, @Value("${api.url}") String apiUrl) {
        super();
        this.httpClientService = httpClientService;
        this.linkProcessor = linkProcessor;
        this.resourceUrlBase = apiUrl + "/" + resourceName;
    }

    @Override
    protected String getResourceName() {
        return resourceName;
    }

    @Override
    protected Set<Resource<Environment>> convertResources(ArrayList<LinkedHashMap> v2s) {
        return v2s.stream().
                map(resource -> {
                    try {
                        Environment environment = convertResource(resource, io.galeb.core.entity.Environment.class);
                        Set<Link> links = linkProcessor.extractLinks(resource, resourceName);
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

    private void fixV1Links(Set<Link> links, Long id) {
        linkProcessor.add(links,"/" + resourceName + "/" + id + "/farms", "farms")
                     .add(links,"/" + resourceName + "/" + id + "/targets", "targets")
                     .remove(links, "rulesordered");
    }

    @Override
    public ResponseEntity<PagedResources<Resource<Environment>>> getSearch(String findType, Map<String, String> queryMap) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PagedResources<Resource<Environment>>> get(Integer size, Integer page) {
        String url = resourceUrlBase +
                (size != null || page != null ? "?" : "") +
                (size != null ? "size=" + size : "") +
                (size != null && page != null ? "&" : "") +
                (page != null ? "page=" + page : "");
        try {
            final Set<Resource<Environment>> v1Environments = convertResources(httpClientService.getResponseListOfMap(url, resourceName));
            int totalElements = v1Environments.size();
            size = size != null ? size : 9999;
            page = page != null ? page : 0;
            final PagedResources.PageMetadata metadata =
                    new PagedResources.PageMetadata(size, page, totalElements, Math.max(1, totalElements / size));
            final PagedResources<Resource<Environment>> pagedResources = new PagedResources<>(v1Environments, metadata, linkProcessor.pagedLinks(resourceName, size, page));
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
            LinkedHashMap resource = httpClientService.getResponseMap(url);
            Set<Link> links = linkProcessor.extractLinks(resource, resourceName);
            linkProcessor.add(links,"/" + resourceName + "/" + id + "/farms", "farms")
                         .add(links,"/" + resourceName + "/" + id + "/targets", "targets")
                         .remove(links, "rulesordered");
            Environment environment = convertResource(resource, io.galeb.core.entity.Environment.class);
            environment.setId(Long.parseLong(id));
            return ResponseEntity.ok(new Resource<>(environment, links));
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<Environment>> post(String body) {
        Environment environment = bodyToV1(body);
        if (environment != null) {
            try {
                Response response = httpClientService.post(resourceUrlBase, body);
                if (!response.hasResponseStatus() || response.getStatusCode() > 299 || (body = response.getResponseBody()) == null || body.isEmpty()) {
                    return ResponseEntity.status(response.getStatusCode()).build();
                }
                LinkedHashMap resource = stringToMap(body);
                Set<Link> links = linkProcessor.extractLinks(resource, resourceName);
                long id = linkProcessor.extractId(links);
                Environment entityConverted = convertResource(resource, io.galeb.core.entity.Environment.class);
                entityConverted.setId(id);
                fixV1Links(links, id);
                String location = "/" + resourceName + "/" + id;
                return ResponseEntity.created(URI.create(location)).body(new Resource<>(entityConverted, links));
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
