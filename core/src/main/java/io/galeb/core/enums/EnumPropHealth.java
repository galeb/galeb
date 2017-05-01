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

package io.galeb.core.enums;

public enum  EnumPropHealth {

    PROP_HEALTHCHECK_RETURN ("hcBody"),
    PROP_HEALTHCHECK_PATH   ("hcPath"),
    PROP_HEALTHCHECK_HOST   ("hcHost"),
    PROP_HEALTHCHECK_CODE   ("hcStatusCode"),
    PROP_HEALTHY            ("healthy"),
    PROP_STATUS_DETAILED    ("status_detailed");

    private final String prop;

    EnumPropHealth(String prop) {
        this.prop = prop;
    }

    public String value() {
        return prop;
    }

    @Override
    public String toString() {
        return prop;
    }
}
