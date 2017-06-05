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
import io.undertow.server.HttpServerExchange;
import org.junit.Before;
import org.mockito.Mockito;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractHostSelectorTest {

    static final int NUM_HOSTS   = 100;
    static final int NUM_RETRIES = 10;

    final Host[] hosts = new Host[NUM_HOSTS];
    final HttpServerExchange commonExchange = new HttpServerExchange(null);

    @Before
    public void setUp() {
        for (int x = 0; x < NUM_HOSTS; x++) {
            final Host host = mock(Host.class, Mockito.withSettings().stubOnly());
            when(host.getUri()).thenReturn(URI.create("http://127.0.0.1:" + x));
            when(host.getOpenConnection()).thenReturn(x);
            hosts[x] = host;
        }
    }

    String getName(Class<? extends HostSelector> klazz) {
        return klazz.getSimpleName().replaceAll("HostSelector", "");
    }

}
