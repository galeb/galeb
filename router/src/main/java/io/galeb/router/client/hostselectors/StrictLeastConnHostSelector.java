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

import java.util.Comparator;
import java.util.Map;

public class StrictLeastConnHostSelector extends ClientStatisticsMarker implements HostSelector {

    @Override
    public int selectHost(final Host[] availableHosts, final HttpServerExchange exchange) {
        int pos = convertToMapStream(availableHosts)
                .sorted(Comparator.comparing(e -> e.getValue().getOpenConnection()))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_POS);
        stamp(availableHosts[pos], exchange);
        return pos;
    }
}
