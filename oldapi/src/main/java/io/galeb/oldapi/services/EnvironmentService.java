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
import io.galeb.oldapi.v1entities.AbstractEntity;
import io.galeb.oldapi.v1entities.Account;
import io.galeb.oldapi.v1entities.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class EnvironmentService {

    private static final Logger LOGGER = LogManager.getLogger(EnvironmentService.class);

    private final String envClassName = Environment.class.getSimpleName().toLowerCase();
    private final ObjectMapper mapper = new ObjectMapper();
    private final AsyncHttpClient httpClient;

    @Autowired
    public EnvironmentService(HttpClientService clientService) {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = clientService.httpClient();
    }

    @SuppressWarnings("unchecked")
    private PagedResources<Resource<Environment>> convertResources(ArrayList<LinkedHashMap> v2Environments) {
        Set<Resource<Environment>> v1Environments = v2Environments.stream().
                map(resource -> {
                    try {
                        io.galeb.core.entity.Environment v1Environment = mapper.readValue(mapper.writeValueAsString(resource), io.galeb.core.entity.Environment.class);
                        Environment environment = new Environment(v1Environment.getName());
                        environment.setId(v1Environment.getId());
                        environment.setStatus(extractStatus());
                        List<Link> links = extractLinks(resource);
                        return new Resource<>(environment, links);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());

        final List<Link> links = new ArrayList<>();
        links.add(new Link("/" + envClassName + "?page=0&size=1000{&sort}", "self"));
        links.add(new Link("/" + envClassName + "/search", "search"));
        final PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(9999, 0, v1Environments.size(), 1);
        return new PagedResources<>(v1Environments, metadata, links);
    }

    @SuppressWarnings("unchecked")
    private List<Link> extractLinks(LinkedHashMap resource) {
        return ((LinkedHashMap<String, Object>) resource.get("_links")).entrySet().stream()
                .map(this::convertLink)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private ArrayList<LinkedHashMap> jsonToList(String body) throws IOException {
        return (ArrayList<LinkedHashMap>) ((LinkedHashMap) mapper.readValue(body, HashMap.class).get("_embedded")).get(envClassName);
    }

    @SuppressWarnings("unchecked")
    private Link convertLink(Map.Entry<String, Object> entry) {
        String href = ((LinkedHashMap<String, String>) entry.getValue()).get("href")
                .replaceAll(".*/" + envClassName, "/" + envClassName);
        return new Link(href, entry.getKey());
    }

    // TODO: Set Environment Status
    private AbstractEntity.EntityStatus extractStatus() {
        return AbstractEntity.EntityStatus.UNKNOWN;
    }

    public ResponseEntity<PagedResources<Resource<Environment>>> get() {
        String url = System.getenv("GALEB_API_URL") + "/" + envClassName + "?size=9999";
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = account.getName();
        String password = extractApiToken(account); // extract token from description
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setRealm(Dsl.basicAuthRealm(username, password).setUsePreemptiveAuth(true));
        requestBuilder.setUrl(url);
        try {
            Response response = httpClient.executeRequest(requestBuilder).get();
            String body;
            if (response.hasResponseStatus() && response.getStatusCode() <= 299 && (body = response.getResponseBody()) != null && !body.isEmpty()) {
                final ArrayList<LinkedHashMap> v2Environments = jsonToList(body);
                final PagedResources<Resource<Environment>> pagedResources = convertResources(v2Environments);
                return ResponseEntity.ok(pagedResources);
            }
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    private String extractApiToken(Account account) {
        return account.getDescription().replaceAll(".*#", "");
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
