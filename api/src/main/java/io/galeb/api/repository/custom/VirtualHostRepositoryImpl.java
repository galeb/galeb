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

import com.google.common.collect.Sets;
import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.VirtualHost;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class VirtualHostRepositoryImpl extends AbstractRepositoryImplementation<VirtualHost> implements VirtualHostRepositoryCustom, WithRoles {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StatusService statusService;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @PostConstruct
    private void init() {
        setEntityManager(em);
        setSimpleJpaRepository(VirtualHost.class, em);
        setStatusService(statusService);
    }

    @Override
    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Sets.newHashSet(((VirtualHost)entity).getEnvironments());
    }

    @Override
    protected long getProjectId(Object criteria) {
        VirtualHost virtualHost = null;
        try {
            if (criteria instanceof VirtualHost) {
                virtualHost = em.find(VirtualHost.class, ((VirtualHost) criteria).getId());
            }
            if (criteria instanceof Long) {
                virtualHost = em.find(VirtualHost.class, criteria);
            }
            if (criteria instanceof String) {
                String query = "SELECT v FROM VirtualHost v WHERE v.name = :name";
                virtualHost = em.createQuery(query, VirtualHost.class).setParameter("name", criteria).getSingleResult();
            }
        } catch (Exception ignored) {}
        if (virtualHost == null) {
            return -1L;
        }
        return virtualHost.getProject().getId();
    }

}
