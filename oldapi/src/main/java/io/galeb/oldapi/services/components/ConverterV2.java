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

package io.galeb.oldapi.services.components;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.WithStatus;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConverterV2 {

    private static final Logger LOGGER = LogManager.getLogger(ConverterV2.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final LinkProcessor linkProcessor;

    private Class<? super AbstractEntity> entityClass;

    @Autowired
    public ConverterV2(LinkProcessor linkProcessor) {
        this.linkProcessor = linkProcessor;
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    String getResourceName() {
        return entityClass.getSimpleName().toLowerCase();
    }

    @SuppressWarnings("unchecked")
    public void setV1Class(Class<?> entityClass) {
        this.entityClass = (Class<? super AbstractEntity>) entityClass;
    }

    private io.galeb.core.entity.AbstractEntity convertFromV2MapToV2(LinkedHashMap map, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) throws IOException {
        return mapper.readValue(mapper.writeValueAsString(map), v2entityClass);
    }

    public void convertFromV2LinksToV1Links(Set<Link> links, Long id) {
        //
    }

    public Set<Resource<?>> convertFromV2ListOfMapsToV1Resources(ArrayList<LinkedHashMap> listOfMapsV2, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
        return listOfMapsV2.stream().
                map(resource -> {
                    try {
                        AbstractEntity<?> v1Entity = convertFromV2MapToV1(resource, v2entityClass);
                        Set<Link> links = linkProcessor.extractLinks(resource, getResourceName());
                        Long id = linkProcessor.extractId(links);
                        convertFromV2LinksToV1Links(links, id);
                        v1Entity.setId(id);
                        return new Resource<>(v1Entity, links);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public AbstractEntity<?> convertFromV2MapToV1(LinkedHashMap v2Map, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) throws IOException {
        Object v2EntityObj = convertFromV2MapToV2(v2Map, v2entityClass);
        io.galeb.core.entity.AbstractEntity v2Entity = v2entityClass.cast(v2EntityObj);
        return convertFromV2ToV1(v2Entity, v2entityClass);
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    public AbstractEntity<?> convertFromV2ToV1(io.galeb.core.entity.AbstractEntity v2Entity, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass) {
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
        AbstractEntity<?> v1Entity = null;
        try {
            v1Entity = (AbstractEntity<?>) entityClass.getConstructor().newInstance();
            v1Entity.setName(v2Name);
            v1Entity.setId(v2Entity.getId());
            v1Entity.setCreatedAt(v2Entity.getCreatedAt());
            v1Entity.setCreatedBy(v2Entity.getCreatedBy());
            v1Entity.setLastModifiedAt(v2Entity.getLastModifiedAt());
            v1Entity.setLastModifiedBy(v2Entity.getLastModifiedBy());
            v1Entity.setVersion(v2Entity.getVersion());
            v1Entity.setStatus(extractV1StatusFromV2(v2Entity));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return v1Entity;
    }

    @SuppressWarnings("Duplicates")
    public AbstractEntity.EntityStatus toV1Status(WithStatus.Status status) {
        switch (status) {
            case OK:
            case DELETED: return AbstractEntity.EntityStatus.OK;
            case PENDING: return AbstractEntity.EntityStatus.PENDING;
            default:      return AbstractEntity.EntityStatus.UNKNOWN;
        }
    }

    private AbstractEntity.EntityStatus extractV1StatusFromV2(io.galeb.core.entity.AbstractEntity entity) {
        WithStatus.Status v2Status = WithStatus.Status.OK;
        if (entity instanceof WithStatus) {
            v2Status = ((WithStatus)entity).getStatus().entrySet().stream().map(Map.Entry::getValue).findAny().orElse(WithStatus.Status.UNKNOWN);
        }
        return toV1Status(v2Status);
    }
}
