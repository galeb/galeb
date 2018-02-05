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

package io.galeb.oldapi.services;

import io.galeb.oldapi.entities.v1.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class RuleService extends AbstractConverterService<Rule> {

    private static final Logger LOGGER = LogManager.getLogger(RuleService.class);

    private static final String[] ADD_DEL = {"parents", "ruleType", "defaultIn"};
    private static final String[] DEL_REL = {"rulesOrdered", "project"};

    @Override
    String[] addRel() {
        return ADD_DEL;
    }

    @Override
    String[] delRel() {
        return DEL_REL;
    }

}
