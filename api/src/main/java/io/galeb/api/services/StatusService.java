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

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.WithStatus.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StatusService {

    @Autowired
    ChangesService changesService;

    public Map<Long, Status> status(AbstractEntity entity) {
        if (entity instanceof Environment) {
            boolean exists = changesService.hasByEnvironmentId(entity.getId());
            Map<Long, Status> mapStatus = new HashMap<>();
            mapStatus.put(entity.getId(), exists ? Status.PENDING : Status.OK);
            return mapStatus;
        }
        if (entity.isQuarantine()) {
            Map<Long, Status> mapStatus = new HashMap<>();
            entity.getAllEnvironments().forEach(e -> mapStatus.put(e.getId(), Status.DELETED));
            return mapStatus;
        }
        Set<Long> allEnvironmentsWithChanges = changesService.listEnvironmentIds(entity);
        Set<Long> allEnvironmentIdsEntity = entity.getAllEnvironments().stream().map(Environment::getId).collect(Collectors.toSet());
        allEnvironmentIdsEntity.removeAll(allEnvironmentsWithChanges);

        Map<Long, Status> mapStatus = new HashMap<>();
        allEnvironmentIdsEntity.forEach(e -> mapStatus.put(e, Status.OK));
        allEnvironmentsWithChanges.forEach(e -> mapStatus.put(e, Status.PENDING));

        return mapStatus;
    }

}
