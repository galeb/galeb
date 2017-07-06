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
import java.util.Set;

@Entity
@Table(name = "ruletype", uniqueConstraints = { @UniqueConstraint(name = "UK_name_ruletype", columnNames = { "name" }) })
public class RuleType extends AbstractEntity<RuleType> {

    private static final long serialVersionUID = 5596582746795373010L;

    @OneToMany(mappedBy = "ruleType")
    private Set<Rule> rules;

    public RuleType(String name) {
        setName(name);
    }

    protected RuleType() {
        //
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public RuleType setRules(Set<Rule> rules) {
        if (rules != null) {
            updateHash();
            this.rules.clear();
            this.rules.addAll(rules);
        }
        return this;
    }
}
