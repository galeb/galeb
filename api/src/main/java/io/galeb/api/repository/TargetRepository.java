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

import io.galeb.api.repository.custom.TargetRepositoryCustom;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Collection;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "target", collectionResourceRel = "target", itemResourceRel = "target")
public interface TargetRepository extends JpaRepository<Target, Long>, TargetRepositoryCustom {

    @Override
    @PreAuthorize("@perm.allowSave(#target, #this)")
    Target save(@Param("target") Target target);

    @Override
    @PreAuthorize("@perm.allowDelete(#id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(#id, #this)")
    Target findOne(@Param("id") Long id);

    @Override
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<Target> findAll(Pageable pageable);

    Page<Target> findByNameAndPoolsIn(@Param("name") String name, @Param("pools") Collection<Pool> pools, Pageable page);
}
