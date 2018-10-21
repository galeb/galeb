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

import io.galeb.api.repository.custom.HealthStatusRepositoryCustom;
import io.galeb.core.entity.HealthStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "healthstatus", collectionResourceRel = "healthstatus", itemResourceRel = "healthstatus")
public interface HealthStatusRepository extends JpaRepository<HealthStatus, Long>, HealthStatusRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(#healthstatus, #this)")
    @Caching(evict = {
            @CacheEvict(value = "cache_projectFromHealthStatusDao", allEntries = true),
            @CacheEvict(value = "cache_entityExist", key = "{ 'exist', 'HealthStatus', #p0.getId() }")
    })
    HealthStatus save(@Param("healthstatus") HealthStatus healthstatus);

    @Override
    @PreAuthorize("@perm.allowDelete(#id, #this)")
    @Caching(evict = {
            @CacheEvict(value = "cache_projectFromHealthStatusDao", allEntries = true),
            @CacheEvict(value = "cache_entityExist", key = "{ 'exist', 'HealthStatus', #p0 }")
    })
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(#id, #this)")
    HealthStatus findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<HealthStatus> findAll(Pageable pageable);
}
