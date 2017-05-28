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

package io.galeb.router.client.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.undertow.server.HttpServerExchange;

import java.nio.charset.Charset;

import static com.google.common.hash.Hashing.consistentHash;
import static com.google.common.hash.Hashing.sipHash24;

public class HashUriPathHostSelector extends ClientStatisticsMarker implements HostSelector {

    @Override
    public int selectHost(final Host[] availableHosts, final HttpServerExchange exchange) {
        int pos = consistentHash(sipHash24().hashString(getKey(exchange), Charset.defaultCharset()), availableHosts.length);
        stamp(availableHosts[pos], exchange);
        return pos;
    }

    private String getKey(final HttpServerExchange exchange) {
        return exchange.getRelativePath();
    }

}
