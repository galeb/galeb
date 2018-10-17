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

import io.galeb.api.dao.GenericDaoService;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.Account;
import io.galeb.core.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class ProjectRepositoryImpl extends AbstractRepositoryImplementation<Project> implements ProjectRepositoryCustom, WithRoles {

    @Autowired
    private GenericDaoService genericDaoService;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Project.class, genericDaoService);
        setStatusService(statusService);
    }

    @Override
    protected long getProjectId(Object criteria) {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (criteria instanceof Project) {
            return ((Project) criteria).getId();
        }
        if (criteria instanceof Long) {
            Project project = (Project) genericDaoService.findOne(Project.class, (Long) criteria);
            if (project == null) {
                return NOT_FOUND;
            }
            return project.getId();
        }
        if (criteria instanceof String) {
            Project project = (Project) genericDaoService.findByName(Project.class, (String) criteria);
            return project.getId();
        }
        return -1;

    }
}
