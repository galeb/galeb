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

import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.VirtualhostGroup;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class VirtualhostGroupRepositoryImpl extends AbstractRepositoryImplementation<VirtualhostGroup> implements VirtualhostGroupRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(VirtualhostGroup.class, em);
        setStatusService(statusService);
    }

    @Override
    protected long getProjectId(Object criteria) {
        VirtualhostGroup virtualhostGroup = null;
        long projectId = -1L;
        try {
            if (criteria instanceof VirtualhostGroup) {
                virtualhostGroup = em.find(VirtualhostGroup.class, ((VirtualhostGroup) criteria).getId());
            }
            if (criteria instanceof Long) {
                virtualhostGroup = em.find(VirtualhostGroup.class, criteria);
                if (virtualhostGroup == null) {
                    return NOT_FOUND;
                }
            }
            if (criteria instanceof Project) {
                projectId = ((Project)criteria).getId();
            }
        } catch (Exception ignored) {}
        if (projectId > -1L) return projectId;
        if (virtualhostGroup == null) return -1L;
        List<Project> projects = em.createNamedQuery("projectFromVirtualhostGroup", Project.class)
                .setParameter("id", virtualhostGroup.getId())
                .getResultList();
        if (projects == null || projects.isEmpty()) return -1L;
        return projects.stream().map(AbstractEntity::getId).findAny().orElse(-1L);
    }

    @Override
    protected String querySuffix(String username) {
        return "INNER JOIN entity.virtualhosts v INNER JOIN v.project.teams t INNER JOIN t.accounts a " +
                "WHERE a.username = '" + username + "'";
    }

}
