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

import io.galeb.core.enums.SystemEnv;
import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import java.nio.charset.Charset;

import static com.google.common.hash.Hashing.consistentHash;
import static com.google.common.hash.Hashing.sipHash24;

public class HashSourceIpHostSelector extends ClientStatisticsMarker implements HostSelector {

    private final boolean ignoreXForwardedFor = Boolean.parseBoolean(SystemEnv.IGNORE_XFORWARDED_FOR.getValue());

    @Override
    public int selectHost(final Host[] availableHosts, final HttpServerExchange exchange) {
        int pos = consistentHash(sipHash24().hashString(getKey(exchange), Charset.defaultCharset()), availableHosts.length);
        stamp(availableHosts[pos], exchange);
        return pos;
    }

    private String getKey(final HttpServerExchange exchange) {
        String aSourceIP;
        String defaultSourceIp = "127.0.0.1";
        String httpHeaderXrealIp = "X-Real-IP";
        String httpHeaderXForwardedFor = "X-Forwarded-For";

        if (exchange == null) {
            return defaultSourceIp;
        }

        if (ignoreXForwardedFor) {
            aSourceIP = exchange.getSourceAddress().getHostString();
        } else {
            final HeaderValues headerXrealIp = exchange.getRequestHeaders().get(httpHeaderXrealIp);
            aSourceIP = headerXrealIp != null ? headerXrealIp.peekFirst() : null;
            if (aSourceIP != null) {
                return aSourceIP;
            }
            final HeaderValues headerXForwardedFor = exchange.getRequestHeaders().get(httpHeaderXForwardedFor);
            aSourceIP = headerXForwardedFor != null ? headerXForwardedFor.peekFirst() : null;
            if (aSourceIP != null) {
                return aSourceIP.contains(",") ? aSourceIP.split(",")[0] : aSourceIP;
            }
            aSourceIP = exchange.getSourceAddress().getHostString();
        }

        return aSourceIP != null ? aSourceIP : defaultSourceIp;
    }

}
