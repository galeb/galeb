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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.reflect.TypeToken;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import io.galeb.oldapi.services.components.ConverterV1;
import io.galeb.oldapi.services.components.ConverterV2;
import io.galeb.oldapi.services.http.HttpClientService;
import io.galeb.oldapi.services.http.Response;
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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public abstract class AbstractConverterService<T extends AbstractEntity> implements LinkProcessor, HttpMethods<T> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractConverterService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<? super T> entityClass = new TypeToken<T>(getClass()){}.getRawType();

    @Autowired
    protected HttpClientService httpClientService;

    @Autowired
    protected ConverterV1 converterV1;

    @Autowired
    protected ConverterV2 converterV2;

    @Value("${api.url}")
    protected String apiUrl;

    protected String resourceUrlBase;

    AbstractConverterService() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    private void init() {
        resourceUrlBase = apiUrl + "/" + getResourceName();
    }

    // internals

    String fullUrlWithSizeAndPage(int size, int page) {
        return resourceUrlBase + "?size=" + size + "&page=" + page;
    }

    int getPageRequest(Map<String, String> queryMap) {
        return Integer.valueOf(Optional.ofNullable(queryMap.get("page")).orElse("0"));
    }

    int getSizeRequest(Map<String, String> queryMap) {
        return Integer.valueOf(Optional.ofNullable(queryMap.get("size")).orElse("99999"));
    }

    // HTTP METHODS

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
                        Set<Link> v2links = new HashSet<>(v2.getLinks());
                        v2LinksToV1Links(v2links, v2.getContent().getId());
                        return new Resource<>(converterV1.v2ToV1(v2.getContent(), v2entityClass, entityClass), v2links);
                    })
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

    @Override
    public ResponseEntity<Resource<? extends AbstractEntity>> post(String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        T entity = convertFromJsonStringToV1(body);
        if (entity != null) {
            try {
                Response response = httpClientService.post(resourceUrlBase, convertFromJsonStringV1ToJsonStringV2(body));
                return processResponse(response, -1, HttpMethod.POST, v2entityClass);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Resource<? extends AbstractEntity>> putWithId(String id, String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        T entity = convertFromJsonStringToV1(body);
        if (entity != null) {
            try {
                Response response = httpClientService.put(resourceUrlBase + "/" + id, convertFromJsonStringV1ToJsonStringV2(body));
                return processResponse(response, Long.parseLong(id), HttpMethod.PUT, v2entityClass);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    // common

    ResponseEntity<Resource<? extends AbstractEntity>> processResponse(Response response, long id, HttpMethod method, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) throws IOException {
        ConverterV2.V2JsonHalData v2JsonHalData = converterV2.toV2JsonHal(response, v2entityClass);
        Set<Link> v2links = converterV2.extractLinks(v2JsonHalData, getResourceName());
        Optional<AbstractEntity> v1Entities = v2JsonHalData.getV2entities().stream().map(v2 -> converterV1.v2ToV1(v2.getContent(), v2entityClass, entityClass)).findAny();
        AbstractEntity entityConverted;
        if (v1Entities.isPresent()) {
            long idEntity = id > -1 ? id : extractIdFromSelfLink(v2links);
            entityConverted = v1Entities.get();
            entityConverted.setId(idEntity);
            v2LinksToV1Links(v2links, idEntity);
        } else {
            throw new RuntimeException("Server error");
        }

        final Resource<AbstractEntity> body = new Resource<>(entityConverted, v2links);
        switch (method) {
            case POST:
                String location = "/" + getResourceName() + "/" + id;
                return ResponseEntity.created(URI.create(location)).body(body);
            case PUT:
                return ResponseEntity.noContent().build();
            case GET:
                return ResponseEntity.ok(body);
        }
        throw new IllegalArgumentException("Method " + method + " not supported");
    }

    PagedResources<Resource<? extends AbstractEntity>> buildPagedResources(int size, int page, Set<Resource<? extends AbstractEntity>> resources) {
        int totalElements = resources.size();
        final PagedResources.PageMetadata metadata =
                new PagedResources.PageMetadata(size, page, totalElements, Math.max(1, totalElements / size));
        return new PagedResources<>(resources, metadata, pagedLinks(getResourceName(), size, page));
    }

    String convertFromObjectToJsonString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    String getResourceName() {
        return entityClass.getSimpleName().toLowerCase();
    }

    // v1

    @SuppressWarnings("unchecked")
    T convertFromJsonStringToV1(String str) {
        try {
            return (T) mapper.readValue(str, entityClass);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    // v1 -> v2

    String convertFromJsonStringV1ToJsonStringV2(String body) {
        // DEFAULT: Body not changed
        return body;
    }

    // v2 -> v1

    String[] addRel() {
        return new String[0];
    }

    String[] delRel() {
        return new String[0];
    }

    void v2LinksToV1Links(Set<Link> v2links, Long id) {
        for (String rel : addRel()) addLink(v2links, "/" + getResourceName() + "/" + id + "/" + rel, rel);
        for (String rel: delRel()) removeLink(v2links, rel);
    }

}
