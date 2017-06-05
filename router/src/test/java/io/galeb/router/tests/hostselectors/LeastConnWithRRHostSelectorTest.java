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

package io.galeb.router.tests.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.HostSelectorLookup;
import io.galeb.router.client.hostselectors.LeastConnWithRRHostSelector;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class LeastConnWithRRHostSelectorTest extends AbstractHostSelectorTest {

    private final HostSelector leastConnWithRRHostSelector = HostSelectorLookup.getHostSelector("LeastConn");

    @Test
    public void testSelectHost() throws Exception {
        assertThat(leastConnWithRRHostSelector, instanceOf(LeastConnWithRRHostSelector.class));
        for (int retry = 1; retry <= NUM_RETRIES; retry++) {
            int hostsLength = new Random().nextInt(NUM_HOSTS);
            final long limit = (int) Math.ceil((float) hostsLength * ((LeastConnWithRRHostSelector)leastConnWithRRHostSelector).getCuttingLine());
            IntStream.range(0, hostsLength - 1).forEach(x -> {
                final Host[] newHosts = Arrays.copyOf(hosts, hostsLength);
                long result = leastConnWithRRHostSelector.selectHost(newHosts, commonExchange);
                assertThat(result, equalTo(x % limit));
            });
            ((LeastConnWithRRHostSelector)leastConnWithRRHostSelector).reset();
        }
    }
} 
