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

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import io.galeb.api.repository.custom.EnvironmentRepositoryCustom;
import io.galeb.core.entity.Environment;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "environment", collectionResourceRel = "environment", itemResourceRel = "environment")
public interface EnvironmentRepository extends JpaRepository<Environment, Long>, EnvironmentRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(#environment, #this)")
    Environment save(@Param("environment") Environment environment);

    @Override
    @PreAuthorize("@perm.allowDelete(#id, #this)")
    void delete(@Param("id") Long id);

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT e FROM Environment as e " +
            "inner join e.pools as p " +
            "inner join p.targets as t " +
            "WHERE t.id = :targetId")
    Set<Environment> findAllByTargetId(@Param("targetId") long targetId);

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT e FROM Environment as e " +
            "inner join e.virtualhosts as v " +
            "inner join v.virtualhostgroup as vp " +
            "inner join vp.rulesordered as ro " +
            "WHERE ro.id = :ruleorderedId")
    Set<Environment> findAllByRuleOrderedId(@Param("ruleorderedId") long ruleorderedId);

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT e FROM Environment as e " +
            "inner join e.virtualhosts as v " +
            "inner join v.virtualhostgroup as vp " +
            "WHERE vp.id = :vhgid")
    @PreAuthorize("@perm.allowView(null , #this)")
    List<Environment> findAllByVirtualhostgroupId(@Param("vhgid") long vhgid);

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT e FROM Environment as e " +
            "inner join e.pools as p " +
            "inner join p.rules as r " +
            "WHERE r.id = :ruleId")
    Set<Environment> findAllByRuleId(@Param("ruleId") long ruleId);

    @Override
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Environment> findAll(Pageable pageable);

    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Environment> findByName(@Param("name") String name, Pageable pageable);

    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Environment> findByNameContaining(@Param("name") String name, Pageable pageable);

    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Environment> findFirst10ByNameContaining(@Param("name") String name, Pageable pageable);
}
