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

import java.util.Set;

public class RuleType extends AbstractEntity<RuleType> {

    private static final long serialVersionUID = 5596582746795373010L;

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
            this.rules.clear();
            this.rules.addAll(rules);
        }
        return this;
    }
}
