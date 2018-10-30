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

package io.galeb.legba.controller;

import io.galeb.legba.repository.EnvironmentRepository;
import org.h2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractController {

    @Autowired
    private EnvironmentRepository environmentRepository;

    protected Long getEnvironmentId(String envname) {
        if (StringUtils.isNumber(envname)) {
            return Long.parseLong(envname);
        } else {
            return environmentRepository.idFromName(envname);
        }
    }
}
