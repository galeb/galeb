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

package io.galeb.api.handler;

import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RuleHandler extends AbstractHandler<Rule> {

    @Autowired
    EnvironmentRepository environmentRepository;

    @Override
    protected Set<Environment> getAllEnvironments(Rule entity) {
        return environmentRepository.findAllByRuleId(entity.getId());
    }
}
