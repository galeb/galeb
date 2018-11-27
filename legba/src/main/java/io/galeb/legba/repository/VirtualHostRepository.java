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

    // @formatter:off

    String FIND_ALL_BY_ENV_ID_SQL =
        "SELECT DISTINCT v FROM VirtualHost as v "
            + "inner join v.environments as e "
            + "WHERE e.id = :envId";

    String FULL_ENTITIES_SQL =
        "SELECT "
            + "v.id as v_id, "                               //0
            + "v.last_modified_at as v_last_modified_at, "   //1
            + "v.name as v_name, "                           //2
            + "ro.last_modified_at as ro_last_modified_at, " //3
            + "ro.rule_order as ro_order, "                  //4
            + "r.last_modified_at as r_last_modified_at, "   //5
            + "r.global as r_global, "                       //6
            + "r.name as r_name, "                           //7
            + "r.matching as r_matching, "                   //8
            + "p.last_modified_at as p_last_modified_at, "   //9
            + "p.name as p_name, "                           //10
            + "p.pool_size as p_pool_size, "                 //11
            + "bp.name as bp_name, "                         //12
            + "t.last_modified_at as t_last_modified_at, "   //13
            + "t.name as t_name, "                           //14
            + "GROUP_CONCAT(hs.last_modified_at) as hs_last_modified_at "  //15
            + "FROM virtualhost v "
            + "INNER JOIN virtualhost_environments v_e on v.id=v_e.virtualhost_id "
            + "INNER JOIN virtualhostgroup vhg on v.virtualhostgroup_id=vhg.id "
            + "INNER JOIN ruleordered ro on ro.virtualhostgroup_ruleordered_id=vhg.id "
            + "INNER JOIN rule r on ro.rule_ruleordered_id=r.id "
            + "INNER JOIN rule_pools rp on rp.rule_id=r.id "
            + "INNER JOIN pool p on rp.pool_id=p.id "
            + "INNER JOIN balancepolicy bp on p.balancepolicy_id=bp.id "
            + "INNER JOIN target t on t.pool_id=p.id "
            + "LEFT OUTER JOIN health_status hs on hs.target_id=t.id "
            + "WHERE v_e.environment_id=:envid AND p.environment_id=:envid AND ro.environment_id=:envid AND "
                    + "NOT (v.quarantine OR ro.quarantine OR r.quarantine OR p.quarantine OR t.quarantine) AND "
                    + "(hs.status IS NULL OR hs.status != 'FAIL') "
            + "GROUP BY hs.last_modified_at";

    // @formatter:on

    @RestResource(exported = false)
    @Query(value = FIND_ALL_BY_ENV_ID_SQL)
    List<VirtualHost> findAllByEnvironmentId(@Param("envId") Long envId);

    @Query(value = FULL_ENTITIES_SQL, nativeQuery = true)
    List<Object[]> fullEntity(@Param("envid") Long envid);

}