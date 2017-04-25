/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2015 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RuleOrder implements Serializable {

    @Column(nullable = false)
    private Long ruleId;

    @Column(nullable = false)
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
