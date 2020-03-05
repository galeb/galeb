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

package io.galeb.api.services;

import io.galeb.core.log.JsonEventToLogger;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Rule;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.WithStatus.Status;
import io.galeb.core.services.ChangesService;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatusService {

    private static final Logger LOGGER = LogManager.getLogger(StatusService.class);

    private static final String FORMAT_KEY_HEALTH = "health:{0}:{1}:{2}";

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ChangesService changesService;

    private int envWithStatusCount(Long envId) {
        String id = String.valueOf(envId);
        final Set<String> envs = new HashSet<String>();
        Set<String> keys = redisTemplate.keys(MessageFormat.format(FORMAT_KEY_HEALTH, id, "*", "*"));
        keys.forEach(key -> {
        	try {
	        	MessageFormat ms = new MessageFormat(FORMAT_KEY_HEALTH);
	            Object[] positions = ms.parse(key);
	            String envIds = (String) positions[0];
	            String healthGroup = (String) positions[1];
	            String localIps = (String) positions[2];
	            envs.add(healthGroup);
        		} catch (Exception e) {
		            JsonEventToLogger errorEvent = new JsonEventToLogger(this.getClass());
		            errorEvent.put("short_message", "Error processing healths - env: " + id);
		            errorEvent.sendError(e);
        		}
           });
        return envs == null ? 0 : envs.size();
    }

    public Map<Long, Status> status(AbstractEntity entity) {
        if (entity instanceof Environment) {
            boolean exists = changesService.hasByEnvironmentId(entity.getId());
            return Collections.singletonMap(entity.getId(), exists ? Status.PENDING : Status.OK);
        }
        final Set<Environment> allEnvironments = entity.getAllEnvironments();
        if (allEnvironments == null || allEnvironments.isEmpty()) {
            if (!(entity instanceof Rule)) {
                LOGGER.error(entity.getClass().getSimpleName() + " ID " + entity.getId() + " is INCONSISTENT. allEnvironments is NULL or Empty");
            }
            return Collections.emptyMap();
        }
        final Boolean isQuarantine;
        if ((isQuarantine = entity.isQuarantine()) != null && isQuarantine) {
            return allEnvironments.stream().collect(Collectors.toMap(Environment::getId, e -> Status.DELETED));
        }
        if (entity instanceof Target && targetHasEnvUnregistered((Target) entity, allEnvironments)) {
            return allEnvironments.stream().collect(Collectors.toMap(Environment::getId, e -> Status.PENDING));
        }
        Set<Long> allEnvironmentsWithChanges = changesService.listEnvironmentIds(entity);
        Set<Long> allEnvironmentIdsEntity = allEnvironments.stream().map(Environment::getId).collect(Collectors.toSet());
        allEnvironmentIdsEntity.removeAll(allEnvironmentsWithChanges);

        Map<Long, Status> mapStatus = new HashMap<>();
        allEnvironmentIdsEntity.forEach(e -> mapStatus.put(e, Status.OK));
        allEnvironmentsWithChanges.forEach(e -> mapStatus.put(e, Status.PENDING));

        return mapStatus;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean targetHasEnvUnregistered(final Target target, final Set<Environment> allEnvironments) {
        final Optional<Environment> anyEnvironment = allEnvironments.stream().findAny();
        int envWithStatus = -1;
        if (anyEnvironment.isPresent()) {
            if (anyEnvironment.get() instanceof Environment) {
                final Environment environment = anyEnvironment.get();
                envWithStatus = envWithStatusCount(environment.getId());
            } else {
                LOGGER.error("Target ID " + target.getId() + " is INCONSISTENT. " +
                        "Is NOT Environment instance of the Environment class ???" +
                        " (real class: " + anyEnvironment.get().getClass() + ")");
            }
        }
        return target.getHealthStatus().size() < envWithStatus;
    }

}
