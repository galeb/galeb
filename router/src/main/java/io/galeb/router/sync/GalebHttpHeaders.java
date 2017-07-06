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

package io.galeb.router.sync;

public enum GalebHttpHeaders {
    ;
    private static final String PREFIX = "X-Galeb-";

    public static final String X_GALEB_GROUP_ID    = PREFIX + "GroupID";
    public static final String X_GALEB_ENVIRONMENT = PREFIX + "Environment";
    public static final String X_GALEB_LOCAL_IP    = PREFIX + "LocalIP";
    public static final String X_GALEB_SHOW_CACHE  = PREFIX + "Show-Cache";
}
