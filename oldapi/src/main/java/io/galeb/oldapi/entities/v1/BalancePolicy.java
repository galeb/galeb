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

import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

public class BalancePolicy extends AbstractEntity<BalancePolicy> {

    private static final long serialVersionUID = 5596582746795373030L;

    private BalancePolicyType balancePolicyType;

    private final Set<Pool> pools = new HashSet<>();

    public BalancePolicy(String name, BalancePolicyType balancePolicyType) {
        Assert.hasText(name, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        Assert.notNull(balancePolicyType, "[Assertion failed] - this argument is required; it must not be null");
        setName(name);
        this.balancePolicyType = balancePolicyType;
    }

    protected BalancePolicy() {
        // Hibernate Requirement
    }

    public BalancePolicyType getBalancePolicyType() {
        return balancePolicyType;
    }

    public BalancePolicy setBalancePolicyType(BalancePolicyType balancePolicyType) {
        Assert.notNull(balancePolicyType, "[Assertion failed] - this argument is required; it must not be null");
        this.balancePolicyType = balancePolicyType;
        return this;
    }

    public Set<Pool> getPools() {
        return pools;
    }

    public BalancePolicy setPools(Set<Pool> pools) {
        if (pools != null) {
            this.pools.clear();
            this.pools.addAll(pools);
        }
        return this;
    }
}
