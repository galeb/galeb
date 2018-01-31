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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.WithStatus;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import io.galeb.oldapi.services.http.HttpClientService;
import io.galeb.oldapi.services.http.Response;
import io.galeb.oldapi.services.utils.LinkProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public abstract class AbstractConverterService<T extends AbstractEntity> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractConverterService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<? super T> entityClass = new TypeToken<T>(getClass()){}.getRawType();
    private final LinkProcessor linkProcessor;
    private final HttpClientService httpClientService;

    protected String resourceUrlBase;

    AbstractConverterService(LinkProcessor linkProcessor, HttpClientService httpClientService) {
        this.linkProcessor = linkProcessor;
        this.httpClientService = httpClientService;

        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    Set<String> validAttributesV1() {
        Set<String> result = new HashSet<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                if (LOGGER.isDebugEnabled()) LOGGER.debug(getResourceName() + ": " + field.getName() + " IGNORED.");
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                if (LOGGER.isDebugEnabled()) LOGGER.debug(getResourceName() + ": " + field.getName() + " IS STATIC. IGNORED.");
                continue;
            }
            if (field.getType().isInstance(Collection.class)) {
                if (LOGGER.isDebugEnabled()) LOGGER.warn(getResourceName() + ": " + field.getName() + " IS COLLECTIONS. Not implemented yet.");
                continue;
            }
            result.add(field.getName());
        }
        return result;
    }

    public ResponseEntity<String> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    public ResponseEntity<PagedResources<Resource<T>>> getSearch(String findType, Map<String, String> queryMap) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<PagedResources<Resource<T>>> get(Integer size, Integer page, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        size = size == null ? 99999 : size;
        page = page == null ? 0 : page;
        String url = resourceUrlBase + "?size=" + size + "&page=" + page;
        try {
            Response response = httpClientService.getResponse(url);
            final Set<Resource<T>> resources = convertResources(extractArrayOfMapsFromBody(getResourceName(), response), v2entityClass);
            final PagedResources<Resource<T>> pagedResources = buildPagedResources(size, page, resources);
            return ResponseEntity.ok(pagedResources);
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<Resource<T>> getWithId(String id, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        String url = resourceUrlBase + "/" + id;
        try {
            Response response = httpClientService.getResponse(url);
            return buildResource(response, Long.parseLong(id), HttpMethod.GET, v2entityClass);
        } catch (InterruptedException | ExecutionException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<Resource<T>> post(String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        T entity = stringToEntityV1(body);
        if (entity != null) {
            try {
                Response response = httpClientService.post(resourceUrlBase, convertFromV1ToV2(body));
                return buildResource(response, -1, HttpMethod.POST, v2entityClass);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<Resource<T>> putWithId(String id, String body, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        T entity = stringToEntityV1(body);
        if (entity != null) {
            try {
                Response response = httpClientService.put(resourceUrlBase + "/" + id, convertFromV1ToV2(body));
                return buildResource(response, Long.parseLong(id), HttpMethod.PUT, v2entityClass);
            } catch (ExecutionException | InterruptedException | IOException e) {
                LOGGER.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<Void> deleteWithId(String id) {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> patchWithId(String id, String body) {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<String> head() {
        return ResponseEntity.noContent().build();
    }

    ResponseEntity<Resource<T>> buildResource(Response response, long id, HttpMethod method, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) throws IOException {
        LinkedHashMap resource = extractMapFromBody(response);
        if (resource.get("error") != null) {
            throw new IOException("HTTP Response FAIL (status:" + response.getStatusCode() + ", error:" + resource.get("error") + ")");
        }
        Set<Link> links = linkProcessor.extractLinks(resource, getResourceName());
        long idEntity = id > -1 ? id : linkProcessor.extractId(links);
        T entityConverted = convertResource(resource, v2entityClass);
        entityConverted.setId(idEntity);
        fixV1Links(links, idEntity);

        final Resource<T> body = new Resource<>(entityConverted, links);
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

    PagedResources<Resource<T>> buildPagedResources(int size, int page, Set<Resource<T>> resources) {
        int totalElements = resources.size();
        final PagedResources.PageMetadata metadata =
                new PagedResources.PageMetadata(size, page, totalElements, Math.max(1, totalElements / size));
        return new PagedResources<>(resources, metadata, linkProcessor.pagedLinks(getResourceName(), size, page));
    }

    void fixV1Links(Set<Link> links, Long id) {
        //
    }

    private io.galeb.core.entity.AbstractEntity mapToV2AbstractEntity(LinkedHashMap resource, Class<? extends io.galeb.core.entity.AbstractEntity> klazz) throws IOException {
        return mapper.readValue(mapper.writeValueAsString(resource), klazz);
    }

    LinkedHashMap stringToMap(String strObj) throws IOException {
        return  mapper.readValue(strObj, LinkedHashMap.class);
    }

    Set<Resource<T>> convertResources(ArrayList<LinkedHashMap> v2s, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        return v2s.stream().
                map(resource -> {
                    try {
                        T entity = convertResource(resource, v2entityClass);
                        Set<Link> links = linkProcessor.extractLinks(resource, getResourceName());
                        Long id = linkProcessor.extractId(links);
                        fixV1Links(links, id);
                        entity.setId(id);
                        return new Resource<>(entity, links);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    T convertResource(LinkedHashMap resource, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) throws IOException {
        Object v2EntityObj = mapToV2AbstractEntity(resource, v2entityClass);
        io.galeb.core.entity.AbstractEntity v2Entity = v2entityClass.cast(v2EntityObj);
        String v2Name;
        if (v2Entity instanceof Account) {
            v2Name = ((Account) v2Entity).getUsername();
        } else {
            try {
                Method getName = v2entityClass.getMethod("getName");
                v2Name = (String) getName.invoke(v2Entity);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                LOGGER.warn(v2entityClass.getSimpleName() + " has not name. Using ID instead.");
                v2Name = String.valueOf(v2Entity.getId());
            }
        }
        T v1Entity = null;
        try {
            v1Entity = (T) entityClass.getConstructor().newInstance();
            v1Entity.setName(v2Name);
            v1Entity.setId(v2Entity.getId());
            v1Entity.setCreatedAt(v2Entity.getCreatedAt());
            v1Entity.setCreatedBy(v2Entity.getCreatedBy());
            v1Entity.setLastModifiedAt(v2Entity.getLastModifiedAt());
            v1Entity.setLastModifiedBy(v2Entity.getLastModifiedBy());
            v1Entity.setVersion(v2Entity.getVersion());
            v1Entity.setStatus(extractStatus(v2Entity));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return v1Entity;
    }

    @SuppressWarnings("unchecked")
    ArrayList<LinkedHashMap> extractArrayOfMapsFromBody(String resourceName, Response response) throws IOException {
        String body = null;
        if (response.hasResponseStatus() && response.getStatusCode() <= 299 && (body = response.getResponseBody()) != null && !body.isEmpty()) {
            return (ArrayList<LinkedHashMap>) ((LinkedHashMap)
                    mapper.readValue(body, HashMap.class).get("_embedded")).get(resourceName);
        }
        throw new IOException("HTTP Response FAIL (status:" + response.getStatusCode() + ", body:" + body + ")");
    }

    @SuppressWarnings("unchecked")
    LinkedHashMap extractMapFromBody(Response response) {
        String body = "";
        try {
            if (response.hasResponseStatus() && response.getStatusCode() <= 299 && (body = response.getResponseBody()) != null && !body.isEmpty()) {
                return mapper.readValue(body, LinkedHashMap.class);
            }
        } catch (IOException e) {
            body = e.getMessage();
        }
        return new LinkedHashMap(ImmutableMap.of("error", Optional.ofNullable(body).orElse("UNKNOWN")));
    }

    String entityToString(Object entity) {

        try {
            return mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    T stringToEntityV1(String str) {
        try {
            return (T) mapper.readValue(str, entityClass);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    String getResourceName() {
        return entityClass.getSimpleName().toLowerCase();
    }

    private AbstractEntity.EntityStatus extractStatus(io.galeb.core.entity.AbstractEntity entity) {
        WithStatus.Status v2Status = WithStatus.Status.OK;
        if (entity instanceof WithStatus) {
            v2Status = ((WithStatus)entity).getStatus().entrySet().stream().map(Map.Entry::getValue).findAny().orElse(WithStatus.Status.UNKNOWN);
        }
        return convertStatus(v2Status);
    }

    private AbstractEntity.EntityStatus convertStatus(WithStatus.Status status) {
        switch (status) {
            case OK:
            case DELETED: return AbstractEntity.EntityStatus.OK;
            case PENDING: return AbstractEntity.EntityStatus.PENDING;
            default:      return AbstractEntity.EntityStatus.UNKNOWN;
        }
    }

    String convertFromV1ToV2(String body) {
        return body;
    }

}
