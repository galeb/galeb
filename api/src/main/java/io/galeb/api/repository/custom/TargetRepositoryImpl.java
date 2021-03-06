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

import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.api.services.GenericDaoService;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.Target;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class TargetRepositoryImpl extends AbstractRepositoryImplementation<Target> implements TargetRepositoryCustom, WithRoles {

    @Autowired
    private GenericDaoService genericDaoService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Target.class, genericDaoService);
        setStatusService(statusService);
    }

    @Override
    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return environmentRepository.findAllByTargetId(entity.getId());
    }

    @Override
    protected long getProjectId(Object criteria) {
        try {
            long id;
            if (criteria instanceof String) {
                Target target = (Target) genericDaoService.findByName(Target.class, (String) criteria);
                if (target != null) {
                    id = target.getId();
                } else {
                    return NOT_FOUND;
                }
            } else {
                id = getIdIfExist(criteria);
            }
            if (id < 1L) {
                return id;
            }
            List<Project> projects = genericDaoService.projectFromTarget(id);
            if (projects == null || projects.isEmpty()) {
                return -1;
            }
            return projects.stream().map(AbstractEntity::getId).findAny().orElse(-1L);
        } catch (Exception ignored) {
            return -1;
        }
    }
}
