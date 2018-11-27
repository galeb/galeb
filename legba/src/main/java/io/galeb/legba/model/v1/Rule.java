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

package io.galeb.legba.model.v1;

import org.springframework.util.Assert;

public class Rule extends AbstractEntity {

    private Pool pool;
    private Boolean global = false;

    private RuleType ruleType;

    public Rule() {}

    public Pool getPool() {
        return pool;
    }

    public Rule setPool(Pool pool) {
        Assert.notNull(pool, "[Assertion failed] - this argument is required; it must not be null");
        updateHash();
        this.pool = pool;
        return this;
    }

    public boolean isGlobal() {
        return global;
    }

    public Rule setGlobal(Boolean global) {
        if (global != null) {
            updateHash();
            this.global = global;
        }
        return this;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

}
