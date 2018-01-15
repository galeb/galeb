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

package io.galeb.oldapi.v1entities;

import org.springframework.util.Assert;

import java.io.Serializable;

public class RuleOrder implements Serializable {

    private Long ruleId;

    private Integer ruleOrder;

    public RuleOrder() {
        this(Long.MIN_VALUE, Integer.MIN_VALUE);
    }

    public RuleOrder(Long ruleId, Integer ruleOrder) {
        Assert.notNull(ruleId, "[Assertion failed] - this argument is required; it must not be null");
        Assert.notNull(ruleOrder, "[Assertion failed] - this argument is required; it must not be null");
        this.ruleId = ruleId;
        this.ruleOrder = ruleOrder;
    }

    public long getRuleId() {
        return ruleId;
    }

    public int getRuleOrder() {
        return ruleOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        RuleOrder ruleOrder = (RuleOrder) o;
        return this.getRuleId() == ruleOrder.getRuleId();
    }

    @Override
    public int hashCode() {
        return ruleId.hashCode();
    }
}
