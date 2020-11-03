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

package io.galeb.router.tests.client;

import static org.junit.Assert.assertThat;

import java.net.URI;

import org.hamcrest.Matchers;
import org.junit.Test;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;

public class ExtendedLoadBalancingProxyClientTest {

    private final ExtendedLoadBalancingProxyClient proxyClient = new ExtendedLoadBalancingProxyClient();

    @Test
    public void testAddHost() {
        proxyClient.addHost(URI.create("http://127.0.0.1:8080"));
        assertThat(proxyClient.isHostsEmpty(), Matchers.equalTo(false));
    }

    @Test
    public void testRemoveHost() {
        proxyClient.addHost(URI.create("http://127.0.0.1:8080"));
        assertThat(proxyClient.isHostsEmpty(), Matchers.equalTo(false));

        proxyClient.removeHost(URI.create("http://127.0.0.1:8080"));
        assertThat(proxyClient.isHostsEmpty(), Matchers.equalTo(true));
    }  
}
