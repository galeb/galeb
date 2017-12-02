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

package io.galeb.router;

import io.undertow.server.HttpHandler;
import io.undertow.util.HttpString;

import static io.undertow.util.HttpString.tryFromString;

@SuppressWarnings("unused")
public enum VirtualHostsNotExpired {
    PING              ("__ping__"),
    CACHE             ("__cache__"),
    INFO              ("__info__");

    private final String host;

    VirtualHostsNotExpired(final String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
