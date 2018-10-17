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
import io.galeb.api.dao.GenericDaoService;
import io.galeb.api.services.StatusService;
import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Set;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
public class PoolRepositoryImpl extends AbstractRepositoryImplementation<Pool> implements PoolRepositoryCustom, WithRoles {

    @Autowired
    private GenericDaoService genericDaoService;

    @Autowired
    private StatusService statusService;

    @PostConstruct
    private void init() {
        setSimpleJpaRepository(Pool.class, genericDaoService);
        setStatusService(statusService);
    }

    @Override
    protected Set<Environment> getAllEnvironments(AbstractEntity entity) {
        return Sets.newHashSet(((Pool)entity).getEnvironment());
    }

    @Override
    protected long getProjectId(Object criteria) {
        Pool pool = null;
        if (criteria instanceof Pool) {
            pool = (Pool) genericDaoService.findOne(Pool.class, ((Pool) criteria).getId());
        }
        if (criteria instanceof Long) {
            pool = (Pool) genericDaoService.findOne(Pool.class, (Long) criteria);
            if (pool == null) {
                return NOT_FOUND;
            }
        }
        if (criteria instanceof String) {
            pool = (Pool) genericDaoService.findByName(Pool.class, (String)criteria);
        }
        return pool != null ? pool.getProject().getId() : -1L;
    }
}
