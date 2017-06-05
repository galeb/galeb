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
import io.galeb.router.client.hostselectors.HashSourceIpHostSelector;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.HostSelectorLookup;
import io.undertow.server.HttpServerExchange;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class HashSourceIpHostSelectorTest extends AbstractHashHostSelectorTest {

    private final HostSelector hashSourceIpHostSelector = HostSelectorLookup.getHostSelector(getName(HashSourceIpHostSelector.class));

    @Test
    public void testSelectHost() throws Exception {
        assertThat(hashSourceIpHostSelector, instanceOf(HashSourceIpHostSelector.class));
        double errorPercentMax = 1.0;
        double limitOfNotHitsPercent = 5.0;
        int numPopulation = 1000;
        doRandomTest(errorPercentMax, limitOfNotHitsPercent, numPopulation);
    }

    @Override
    int getResult(HttpServerExchange exchange, Host[] newHosts) {
        return hashSourceIpHostSelector.selectHost(newHosts, exchange);
    }

    @Override
    void changeExchange(HttpServerExchange exchange, int x) {
        InetSocketAddress address = InetSocketAddress.createUnresolved(ipIntToDotted(x), 10000 + (x % 40000));
        exchange.setSourceAddress(address);
    }

    private String ipIntToDotted(int x) {
        return (x & 0xff000000) / 0x1000000 + "." +
               (x & 0x00ff0000) / 0x10000 + "." +
               (x & 0x0000ff00) / 0x100 + "." +
               (x & 0x000000ff); // 0x1
    }
} 
