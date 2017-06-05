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
import io.galeb.router.client.hostselectors.HashUriPathHostSelector;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.HostSelectorLookup;
import io.undertow.server.HttpServerExchange;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class HashUriPathHostSelectorTest extends AbstractHashHostSelectorTest {

    private final HostSelector hashUriPathHostSelector = HostSelectorLookup.getHostSelector(getName(HashUriPathHostSelector.class));

    @Test
    public void testSelectHost() throws Exception {
        assertThat(hashUriPathHostSelector, instanceOf(HashUriPathHostSelector.class));
        double errorPercentMax = 10.0;
        double limitOfNotHitsPercent = 5.0;
        int numPopulation = 20;
        doRandomTest(errorPercentMax, limitOfNotHitsPercent, numPopulation);
    }

    @Override
    int getResult(HttpServerExchange exchange, Host[] newHosts) {
        return hashUriPathHostSelector.selectHost(newHosts, exchange);
    }

    @Override
    void changeExchange(HttpServerExchange exchange, int x) {
        exchange.setRelativePath("/" + UUID.randomUUID().toString());
    }

}
