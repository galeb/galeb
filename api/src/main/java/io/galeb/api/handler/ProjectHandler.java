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

package io.galeb.api.handler;

import io.galeb.api.repository.RoleGroupRepository;
import io.galeb.core.entity.Project;
import io.galeb.core.entity.RoleGroup;
import io.galeb.core.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectHandler extends AbstractHandler<Project> {

    public static final String ROLE_PROJECT_DEFAULT = "PROJECT_DEFAULT";

    @Autowired
    private RoleGroupRepository roleGroupRepository;

    @Override
    protected void onBeforeCreate(Project entity) {
        super.onBeforeCreate(entity);
        if (entity.getTeams() == null || entity.getTeams().isEmpty()) {
            throw new BadRequestException("Team(s) undefined");
        }
    }

    @Override
    protected void onAfterCreate(Project entity) {
        super.onAfterCreate(entity);
        RoleGroup roleGroup = roleGroupRepository.findByName(ROLE_PROJECT_DEFAULT);
        roleGroup.getProjects().add(entity);
        roleGroupRepository.saveByPass(roleGroup);
    }
}
