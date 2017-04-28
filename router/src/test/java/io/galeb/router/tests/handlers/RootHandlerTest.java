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

package io.galeb.router.tests.handlers;

import io.galeb.core.entity.Pool;
import io.galeb.core.rest.ManagerClient;
import io.galeb.router.configurations.ManagerClientCacheConfiguration.ManagerClientCache;
import io.galeb.router.handlers.PathGlobHandler;
import io.galeb.router.handlers.PoolHandler;
import io.galeb.router.handlers.RuleTargetHandler;
import io.galeb.router.services.ExternalDataService;
import io.galeb.router.kv.ExternalData;
import io.galeb.router.services.UpdateService;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RootHandlerTest {

    private final NameVirtualHostHandler nameVirtualHostHandler = new NameVirtualHostHandler();
    private final ApplicationContext context = mock(ApplicationContext.class);
    private final ExternalDataService externalData = mock(ExternalDataService.class);
    private final ManagerClientCache cache = mock(ManagerClientCache.class);
    private final ManagerClient managerClient = mock(ManagerClient.class);
    private final UpdateService updateService = new UpdateService(nameVirtualHostHandler, managerClient, cache);
    @Before
    public void setUp() {
        when(externalData.exist(anyString())).thenReturn(true);
        when(externalData.node(anyString())).thenAnswer(invocationOnMock -> {
            String key = invocationOnMock.getArgumentAt(0, String.class);
            ExternalData node = mock(ExternalData.class);

            switch (key) {
                case "": {
                    return node;
                }
                default: {
                    return ExternalData.Generic.NULL.instance();
                }
            }
        });
        when(context.getBean(anyString())).thenReturn(new PoolHandler(mock(Pool.class)));
    }

    @Test
    public void testForceVirtualhostUpdate() {
        final String virtualhost = "test.com";

        nameVirtualHostHandler.addHost(virtualhost, exchange -> {});
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(virtualhost));

        updateService.updateCache(virtualhost);
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(0));
    }

    @Test
    public void testForceAllUpdate() {
        final String virtualhost = "test.com";

        nameVirtualHostHandler.addHost(virtualhost, exchange -> {});
        nameVirtualHostHandler.addHost(virtualhost + "1", exchange -> {});
        nameVirtualHostHandler.addHost(virtualhost + "2", exchange -> {});
        nameVirtualHostHandler.addHost(virtualhost + "3", exchange -> {});
        nameVirtualHostHandler.addHost(virtualhost + "4", exchange -> {});
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(5));

//        updateService.forceUpdateAll();
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(0));
    }

    @Test
    public void testIgnoreForceVirtualhostUpdateIfPingHost() {
        String pingHost = "__ping__";

        nameVirtualHostHandler.addHost(pingHost, exchange -> {});
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(pingHost));

        updateService.updateCache(pingHost);
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(pingHost));
    }

    @Test
    public void testForcePoolUpdate() {
        final String virtualhost = "test.com";

        PathGlobHandler pathGlobHandler = new PathGlobHandler();
        RuleTargetHandler ruleTargetHandler = mock(RuleTargetHandler.class);
        PoolHandler poolHandler = mock(PoolHandler.class);
        Pool pool = mock(Pool.class);
        when(poolHandler.getPool()).thenReturn(pool);
        when(ruleTargetHandler.getNext()).thenReturn(pathGlobHandler);
        pathGlobHandler.addPath("/", 0, poolHandler);

        nameVirtualHostHandler.addHost(virtualhost, ruleTargetHandler);
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(virtualhost));

//        updateService.forcePoolUpdate(pool.getId());
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(0));
    }

    @Test
    public void testForcePoolUpdateByIpAclHandler() {
        final String virtualhost = "test.com";

        PathGlobHandler pathGlobHandler = new PathGlobHandler();
        IPAddressAccessControlHandler ipAddressAccessControlHandler = new IPAddressAccessControlHandler().setNext(pathGlobHandler);
        RuleTargetHandler ruleTargetHandler = mock(RuleTargetHandler.class);
        PoolHandler poolHandler = mock(PoolHandler.class);
        pathGlobHandler.addPath("/", 0, poolHandler);
        Pool pool = mock(Pool.class);
        when(poolHandler.getPool()).thenReturn(pool);
        when(ruleTargetHandler.getNext()).thenReturn(ipAddressAccessControlHandler);

        nameVirtualHostHandler.addHost(virtualhost, ruleTargetHandler);
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(virtualhost));

//        updateService.forcePoolUpdate(pool.getId());
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(0));
    }
}
