/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

package io.galeb.core.entity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "balancepolicytype", uniqueConstraints = { @UniqueConstraint(name = "UK_name_balancepolicytype", columnNames = { "name" }) })
public class BalancePolicyType extends AbstractEntity<BalancePolicyType> {

    private static final long serialVersionUID = 5596582746795373010L;

    @OneToMany(mappedBy = "balancePolicyType")
    private final Set<BalancePolicy> balancePolicies = new HashSet<>();

    public BalancePolicyType(String name) {
        setName(name);
    }

    protected BalancePolicyType() {
        // Hibernate Requirement
    }

    public Set<BalancePolicy> getBalancePolicies() {
        return balancePolicies;
    }

    public BalancePolicyType setBalancePolicies(Set<BalancePolicy> balancePolicies) {
        if (balancePolicies != null) {
            updateHash();
            this.balancePolicies.clear();
            this.balancePolicies.addAll(balancePolicies);
        }
        return this;
    }
}
