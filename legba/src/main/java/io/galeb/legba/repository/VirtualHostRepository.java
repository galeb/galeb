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

package io.galeb.legba.repository;

import io.galeb.core.entity.VirtualHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(exported = false, path = "virtualhost", collectionResourceRel = "virtualhost", itemResourceRel = "virtualhost")
public interface VirtualHostRepository extends JpaRepository<VirtualHost, Long> {

    @RestResource(exported = false)
    @Query(value = "SELECT DISTINCT v FROM VirtualHost as v " +
            "inner join v.environments as e " +
            "WHERE e.id = :envId")
    List<VirtualHost> findAllByEnvironmentId(@Param("envId") Long envId);



}