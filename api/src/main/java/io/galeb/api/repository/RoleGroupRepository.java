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

package io.galeb.api.repository;

import io.galeb.api.repository.custom.RoleGroupRepositoryCustom;
import io.galeb.core.entity.RoleGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "rolegroup", collectionResourceRel = "rolegroup", itemResourceRel = "rolegroup")
public interface RoleGroupRepository extends JpaRepository<RoleGroup, Long>, RoleGroupRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(principal, #rolegroup, #this)")
    RoleGroup save(@Param("rolegroup") RoleGroup rolegroup);

    @Override
    @PreAuthorize("@perm.allowDelete(principal, #id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, #id, #this)")
    RoleGroup findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(principal, principal, #this)")
    @Query("SELECT rg FROM RoleGroup rg INNER JOIN rg.accounts a WHERE a.username LIKE ?#{principal.username == @localAdmin.username ? '%' : principal.username}")
    Page<RoleGroup> findAll(Pageable pageable);
}
