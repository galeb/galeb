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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.galeb.oldapi.entities.v1.Environment;
import io.galeb.oldapi.services.http.HttpClientService;
import io.galeb.oldapi.services.http.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class EnvironmentService extends AbstractConverterService<Environment> {

    private static final Logger LOGGER = LogManager.getLogger(EnvironmentService.class);

    private final String resourceName = Environment.class.getSimpleName().toLowerCase();
    private final String resourceUrlBase = System.getenv("GALEB_API_URL") + "/" + resourceName;
    private final HttpClientService httpClientService;

    @Autowired
    public EnvironmentService(HttpClientService httpClientService) {
        super();
        this.httpClientService = httpClientService;
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
                        Environment environment = convertResource(resource);
                        List<Link> links = extractLinks(resource);
                        return new Resource<>(environment, links);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    protected Environment convertResource(LinkedHashMap resource) throws IOException {
        io.galeb.core.entity.Environment v1Environment = mapper.readValue(mapper.writeValueAsString(resource), io.galeb.core.entity.Environment.class);
        Environment environment = new Environment(v1Environment.getName());
        environment.setStatus(extractStatus());
        return environment;
    }

    public ResponseEntity<PagedResources<Resource<Environment>>> get(Integer size, Integer page) {
        String url = resourceUrlBase +
                (size != null || page != null ? "?" : "") +
                (size != null ? "size=" + size : "") +
                (size != null && page != null ? "&" : "") +
                (page != null ? "page=" + page : "");
        try {
            final Response response = httpClientService.getResponse(url);
            final String body;
            if (response.hasResponseStatus() && response.getStatusCode() <= 299 && (body = response.getResponseBody()) != null && !body.isEmpty()) {
                final Set<Resource<Environment>> v1Environments = convertResources(jsonToList(body));
                int totalElements = v1Environments.size();
                size = size != null ? size : 9999;
                page = page != null ? page : 0;
                final PagedResources.PageMetadata metadata =
                        new PagedResources.PageMetadata(size, page, totalElements, Math.max(1, totalElements / size));
                final PagedResources<Resource<Environment>> pagedResources = new PagedResources<>(v1Environments, metadata, getBaseLinks());
                return ResponseEntity.ok(pagedResources);
            }
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<Resource<Environment>> getWithParam(String id) {
        String url = resourceUrlBase + "/" + id;
        try {
            final Response response = httpClientService.getResponse(url);
            final String body;
            if (response.hasResponseStatus() && response.getStatusCode() <= 299 && (body = response.getResponseBody()) != null && !body.isEmpty()) {
                LinkedHashMap resource = mapper.readValue(body, LinkedHashMap.class);
                return ResponseEntity.ok(new Resource<>(convertResource(resource), Collections.emptyList()));
            }
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }
    
    public ResponseEntity<String> post(String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> postWithParam(String id, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase() + "/" + id, body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> put(String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> putWithParam(String id, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase() + "/" + id, body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> delete() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> deleteWithParam(String id) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), id);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> patch(String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> patchWithParam(String id, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase() + "/" + id, body);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> options() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> optionsWithParam(String id) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), id);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> head() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> headWithParam(String id) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), id);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> trace() {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), "NULL");
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

    public ResponseEntity<String> traceWithParam(String id) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), id);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

}
