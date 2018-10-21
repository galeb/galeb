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

package io.galeb.api.repository.custom;

import io.galeb.api.services.GenericDaoService;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class HealthStatusRepositoryImpl extends AbstractRepositoryImplementation<HealthStatus> implements HealthStatusRepositoryCustom, WithRoles {

    @Autowired
    private GenericDaoService genericDaoService;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(HealthStatus.class, genericDaoService);
        setStatusService(statusService);
    }

    @Override
    protected long getProjectId(Object criteria) {
        long id = -1L;
        try {
            id = getIdIfExist(criteria);
        } catch (Exception ignored) {}
        if (id < 1L) {
            return id;
        }
        List<Project> projects = genericDaoService.projectFromHealthStatus(id);
        if (projects == null || projects.isEmpty()) {
            return -1;
        }
        return projects.stream().map(AbstractEntity::getId).findAny().orElse(-1L);
    }
}
