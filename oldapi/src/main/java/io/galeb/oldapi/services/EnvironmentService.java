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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.oldapi.v1entities.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnvironmentService {

    private static final Logger LOGGER = LogManager.getLogger(EnvironmentService.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RestTemplate restTemplate;

    public EnvironmentService() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ResponseEntity<PagedResources<Resource<Environment>>> get() {
        final Map<String, Integer> params = new HashMap<>();
        params.put("size", 9999);
        String url = "https://api.galeb.globoi.com/environment?size={size}";

        // TODO: Missing ID property
        final ResponseEntity<PagedResources<Resource<Environment>>> response =
                restTemplate.exchange(url,
                                      HttpMethod.GET,
                                      null,
                                      new TypeReferences.PagedResourcesType<Resource<Environment>>(){},
                                      params);
        final PagedResources<Resource<Environment>> responseBody = response.getBody();

        // entities proxied
        Set<Resource<Environment>> environments = responseBody.getContent().stream().map(resource -> {
            Environment environment = resource.getContent();
            List<Link> links = resource.getLinks();
            return new Resource<>(environment, links);
        }).collect(Collectors.toSet());

        final List<Link> links = new ArrayList<>();
        links.add(new Link("https://xxxx/environment?page=0&size=1000{&sort}","self"));
        links.add(new Link("https://xxxx/environment/search", "search"));
        final PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(9999, 0, environments.size(),1);
        final PagedResources<Resource<Environment>> resources = new PagedResources<>(environments, metadata, links);

        return ResponseEntity.ok(resources);
    }

    public ResponseEntity<String> getWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), param);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
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

    public ResponseEntity<String> postWithParam(String param, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase() + "/" + param, body);
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

    public ResponseEntity<String> putWithParam(String param, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase() + "/" + param, body);
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

    public ResponseEntity<String> deleteWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), param);
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

    public ResponseEntity<String> patchWithParam(String param, String body) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase() + "/" + param, body);
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

    public ResponseEntity<String> optionsWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), param);
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

    public ResponseEntity<String> headWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), param);
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

    public ResponseEntity<String> traceWithParam(String param) {
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put(Environment.class.getSimpleName().toLowerCase(), param);
        try {
            return ResponseEntity.ok(mapper.writeValueAsString(emptyMap));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().body("{}");
    }

}
