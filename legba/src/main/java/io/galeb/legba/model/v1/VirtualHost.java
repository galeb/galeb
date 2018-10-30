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

import java.util.HashSet;
import java.util.Set;

public class VirtualHost extends AbstractEntity {

    public VirtualHost() {}

    private final Set<Rule> rules = new HashSet<>();

    private Environment environment;

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        if (rules != null) {
            updateHash();
            this.rules.clear();
            this.rules.addAll(rules);
        }
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
