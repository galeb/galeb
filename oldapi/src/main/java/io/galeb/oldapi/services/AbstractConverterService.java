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
import com.google.common.reflect.TypeToken;
import io.galeb.core.entity.WithStatus;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class AbstractConverterService<T extends AbstractEntity> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractConverterService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<? super T> entityClass = new TypeToken<T>(getClass()){}.getRawType();

    AbstractConverterService() {
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected io.galeb.core.entity.AbstractEntity mapToV2AbstractEntity(LinkedHashMap resource, Class<? extends io.galeb.core.entity.AbstractEntity> klazz) throws IOException {
        return mapper.readValue(mapper.writeValueAsString(resource), klazz);
    }

    protected String getEmptyMap() throws JsonProcessingException {
        return getEmptyMap(null);
    }

    protected String getEmptyMap(String body) throws JsonProcessingException {
        return getEmptyMap(null, body);
    }

    protected String getEmptyMap(Long id, String body) throws JsonProcessingException {
        Map<String, Object> emptyMap = new HashMap<>();
        body = body != null ? body : "NULL";
        emptyMap.put(entityClass.getSimpleName().toLowerCase() + (id != null ? "/" + id : ""), body);
        return mapper.writeValueAsString(emptyMap);
    }

    protected abstract Set<Resource<T>> convertResources(ArrayList<LinkedHashMap> v2s);

    @SuppressWarnings("unchecked")
    protected T convertResource(LinkedHashMap resource, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) throws IOException {
        Object v2EntityObj = mapToV2AbstractEntity(resource, v2entityClass);
        io.galeb.core.entity.AbstractEntity v2Entity = v2entityClass.cast(v2EntityObj);
        String v2Name;
        try {
            Method getName = v2entityClass.getMethod("getName");
            v2Name = (String) getName.invoke(v2Entity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            LOGGER.warn(v2entityClass.getSimpleName() + " has not name. Using ID instead.");
            v2Name = String.valueOf(v2Entity.getId());
        }
        T v1Entity = null;
        try {
            v1Entity = (T) entityClass.getConstructor(String.class).newInstance(v2Name);
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

    protected abstract String getResourceName();

    protected abstract ResponseEntity<PagedResources<Resource<T>>> getSearch(String findType, Map<String, String> queryMap);

    protected abstract ResponseEntity<PagedResources<Resource<T>>> get(Integer size, Integer page);

    protected abstract ResponseEntity<Resource<T>> getWithId(String id);

    protected abstract ResponseEntity<String> post(String body);

    protected abstract ResponseEntity<String> postWithId(String id, String body);

    protected abstract ResponseEntity<String> put(String body);

    protected abstract ResponseEntity<String> putWithId(String id, String body);

    protected abstract ResponseEntity<String> delete();

    protected abstract ResponseEntity<String> deleteWithId(String id);

    protected abstract ResponseEntity<String> patch(String body);

    protected abstract ResponseEntity<String> patchWithId(String id, String body);

    protected abstract ResponseEntity<String> options();

    protected abstract ResponseEntity<String> optionsWithId(String id);

    protected abstract ResponseEntity<String> head();

    protected abstract ResponseEntity<String> headWithId(String id);

    protected abstract ResponseEntity<String> trace();

    protected abstract ResponseEntity<String> traceWithId(String id);

    AbstractEntity.EntityStatus extractStatus(io.galeb.core.entity.AbstractEntity entity) {
        WithStatus.Status v2Status = WithStatus.Status.OK;
        if (entity instanceof WithStatus) {
            v2Status = ((WithStatus)entity).getStatus().entrySet().stream().map(Map.Entry::getValue).findAny().orElse(WithStatus.Status.UNKNOWN);
        }
        return convertStatus(v2Status);
    }

    AbstractEntity.EntityStatus convertStatus(WithStatus.Status status) {
        switch (status) {
            case OK:      return AbstractEntity.EntityStatus.OK;
            case DELETED: return AbstractEntity.EntityStatus.OK;
            case PENDING: return AbstractEntity.EntityStatus.PENDING;
            case UNKNOWN: return AbstractEntity.EntityStatus.UNKNOWN;
            default:      return AbstractEntity.EntityStatus.UNKNOWN;
        }
    }

}
