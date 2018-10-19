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

import io.galeb.api.repository.custom.PoolRepositoryCustom;
import io.galeb.core.entity.Pool;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "pool", collectionResourceRel = "pool", itemResourceRel = "pool")
public interface PoolRepository extends JpaRepository<Pool, Long>, PoolRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(#pool, #this)")
    @Caching(evict = {
        @CacheEvict(value = "cache_projectFromHealthStatusDao", allEntries = true),
        @CacheEvict(value = "cache_projectFromTargetDao", allEntries = true)
    })
    Pool save(@Param("pool") Pool pool);

    @Override
    @PreAuthorize("@perm.allowDelete(#id, #this)")
    @Caching(evict = {
        @CacheEvict(value = "cache_projectFromHealthStatusDao", allEntries = true),
        @CacheEvict(value = "cache_projectFromTargetDao", allEntries = true)
    })
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(#id, #this)")
    Pool findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Pool> findAll(Pageable pageable);

    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Pool> findByName(@Param("name") String name, Pageable pageable);

    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Pool> findByNameContaining(@Param("name") String name, Pageable pageable);

}
