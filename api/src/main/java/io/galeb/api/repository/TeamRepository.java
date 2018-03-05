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
import io.galeb.api.repository.custom.TeamRepositoryCustom;
import io.galeb.core.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;


@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "team", collectionResourceRel = "team", itemResourceRel = "team")
public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowSave(#team, #this)")
    Team save(@Param("team") Team team);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowDelete(#id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(#id, #this)")
    Team findOne(@Param("id") Long id);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Team> findAll(Pageable pageable);

    Team findByName(@Param("name") String var1);

    Iterable<Team> findByNameContaining(@Param("name") String name);

//    Iterable<Team> findByNameContainingWithSize(@Param("name") String name, @Param("size") int size);
}

