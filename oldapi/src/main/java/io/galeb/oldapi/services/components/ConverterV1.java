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

import io.galeb.core.entity.Account;
import io.galeb.core.entity.WithStatus;
import io.galeb.oldapi.entities.v1.AbstractEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Component
public class ConverterV1 {

    private static final Logger LOGGER = LogManager.getLogger(ConverterV1.class);

    public AbstractEntity v2ToV1(io.galeb.core.entity.AbstractEntity v2Entity, Class<? extends io.galeb.core.entity.AbstractEntity> v2entityClass, Class<?> entityClass) {
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
        AbstractEntity v1Entity = null;
        try {
            v1Entity = (AbstractEntity) entityClass.getConstructor().newInstance();
            v1Entity.setName(v2Name);
            v1Entity.setId(v2Entity.getId());
            v1Entity.setCreatedAt(v2Entity.getCreatedAt());
            v1Entity.setCreatedBy(v2Entity.getCreatedBy());
            v1Entity.setLastModifiedAt(v2Entity.getLastModifiedAt());
            v1Entity.setLastModifiedBy(v2Entity.getLastModifiedBy());
            v1Entity.setVersion(v2Entity.getVersion());
            v1Entity.setStatus(v2StatusToV1Status(v2Entity));
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return v1Entity;
    }

    private AbstractEntity.EntityStatus v2StatusToV1Status(io.galeb.core.entity.AbstractEntity v2entity) {
        WithStatus.Status v2Status = WithStatus.Status.OK;
        if (v2entity instanceof WithStatus) {
            v2Status = ((WithStatus)v2entity).getStatus().entrySet().stream().map(Map.Entry::getValue).findAny().orElse(WithStatus.Status.UNKNOWN);
        }
        switch (v2Status) {
            case OK:
            case DELETED: return AbstractEntity.EntityStatus.OK;
            case PENDING: return AbstractEntity.EntityStatus.PENDING;
            default:      return AbstractEntity.EntityStatus.UNKNOWN;
        }
    }

}
