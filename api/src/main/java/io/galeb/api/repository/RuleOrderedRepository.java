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
import io.galeb.api.repository.custom.RuleOrderedRepositoryCustom;
import io.galeb.core.entity.RuleOrdered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@SuppressWarnings({"unused", "unchecked"})
@RepositoryRestResource(path = "ruleordered", collectionResourceRel = "ruleordered", itemResourceRel = "ruleordered")
public interface RuleOrderedRepository extends JpaRepository<RuleOrdered, Long>, RuleOrderedRepositoryCustom {

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowSave(#ruleordered, #this)")
    RuleOrdered save(@Param("ruleordered") RuleOrdered ruleordered);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowDelete(#id, #this)")
    void delete(@Param("id") Long id);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(#id, #this)")
    RuleOrdered findOne(@Param("id") Long id);

    @Override
    @ExposeFilterSwagger
    @PreAuthorize("@perm.allowView(null , #this)")
    Page<RuleOrdered> findAll(Pageable pageable);
}
