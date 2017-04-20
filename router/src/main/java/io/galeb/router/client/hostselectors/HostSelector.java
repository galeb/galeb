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
import io.undertow.util.AttachmentKey;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface HostSelector {

    AttachmentKey<String> REAL_DEST = AttachmentKey.create(String.class);

    int DEFAULT_POS = 0;

    int selectHost(Host[] availableHosts, HttpServerExchange exchange);

    default Stream<Map.Entry<Integer, Host>> convertToMapStream(final Host[] availableHosts) {
        return IntStream.range(0, availableHosts.length)
                .boxed()
                .collect(Collectors.toMap(i -> i, i -> availableHosts[i]))
                .entrySet()
                .stream();
    }
}
