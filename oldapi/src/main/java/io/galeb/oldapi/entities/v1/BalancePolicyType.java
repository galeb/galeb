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

package io.galeb.oldapi.entities.v1;


import java.util.HashSet;
import java.util.Set;

public class BalancePolicyType extends AbstractEntity<BalancePolicyType> {

    private static final long serialVersionUID = 5596582746795373010L;

    private final Set<BalancePolicy> balancePolicies = new HashSet<>();

    public BalancePolicyType(String name) {
        setName(name);
    }

    public BalancePolicyType() {
        // Hibernate Requirement
    }

    public Set<BalancePolicy> getBalancePolicies() {
        return balancePolicies;
    }

    public BalancePolicyType setBalancePolicies(Set<BalancePolicy> balancePolicies) {
        if (balancePolicies != null) {
            this.balancePolicies.clear();
            this.balancePolicies.addAll(balancePolicies);
        }
        return this;
    }
}
