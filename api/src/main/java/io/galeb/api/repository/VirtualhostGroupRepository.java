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

import io.galeb.api.annotations.ExposeFilterSwagger;
import io.galeb.api.repository.custom.VirtualhostGroupRepositoryCustom;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.entity.VirtualhostGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collection;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "virtualhostgroup", collectionResourceRel = "virtualhostgroup", itemResourceRel = "virtualhostgroup")
public interface VirtualhostGroupRepository extends JpaRepository<VirtualhostGroup, Long>, VirtualhostGroupRepositoryCustom {

    @Override
    @ExposeFilterSwagger
    @RestResource(exported = false)
    @PreAuthorize("@perm.allowSave(#virtualhostgroup, #this)")
    VirtualhostGroup save(@Param("virtualhostgroup") VirtualhostGroup virtualhostgroup);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowDelete(#id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(#id, #this)")
    VirtualhostGroup findOne(@Param("id") Long id);

    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(null , #this)")
    VirtualhostGroup findByVirtualhostsIn(@Param("virtualhosts") Collection<VirtualHost> virtualhosts);

    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(null , #this)")
    VirtualhostGroup findByVirtualhosts_Id(@Param("vid") Long vid);

    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(null , #this)")
    VirtualhostGroup findByVirtualhosts_Name(@Param("name") String name);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<VirtualhostGroup> findAll(Pageable pageable);

    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<VirtualhostGroup> findByVirtualhosts_NameContaining(@Param("name") String name, Pageable pageable);
}
