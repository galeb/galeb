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

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class LeastConnWithRRHostSelector extends ClientStatisticsMarker implements HostSelector {

    @SuppressWarnings("WeakerAccess")
    public static final AttachmentKey<Float> CUTTING_LINE_ATTACH = AttachmentKey.create(Float.class);

    private ConcurrentLinkedQueue<Integer> leastConnList = null;

    private float cuttingLine = 0.666f;

    @Override
    public int selectHost(final Host[] availableHosts, final HttpServerExchange exchange) {
        if (leastConnList == null || leastConnList.isEmpty()) {
            Float tempCuttingLine = exchange.getAttachment(CUTTING_LINE_ATTACH);
            cuttingLine = tempCuttingLine != null ? tempCuttingLine : cuttingLine;

            final long limit = (int) Math.ceil((float) availableHosts.length * cuttingLine);
            leastConnList = convertToMapStream(availableHosts)
                            .sorted(Comparator.comparing(e -> e.getValue().getOpenConnection()))
                            .limit(limit)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
        }
        Integer pos = leastConnList.poll();
        if (pos != null) {
            stamp(availableHosts[pos], exchange);
            return pos;
        }
        return selectHost(availableHosts, exchange);
    }

    public float getCuttingLine() {
        return cuttingLine;
    }

    // Test only
    public synchronized void reset() {
        if (leastConnList != null) {
            leastConnList.clear();
        }
    }
}
