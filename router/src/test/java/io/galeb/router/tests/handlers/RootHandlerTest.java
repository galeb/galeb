package io.galeb.router.tests.handlers;

import io.galeb.router.client.etcd.EtcdGenericNode;
import io.galeb.router.completionListeners.AccessLogCompletionListener;
import io.galeb.router.completionListeners.StatsdCompletionListener;
import io.galeb.router.handlers.PathGlobHandler;
import io.galeb.router.handlers.PoolHandler;
import io.galeb.router.handlers.RootHandler;
import io.galeb.router.handlers.RuleTargetHandler;
import io.galeb.router.services.ExternalData;
import io.galeb.router.services.StatsdClient;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.zalando.boot.etcd.EtcdNode;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RootHandlerTest {

    private final NameVirtualHostHandler nameVirtualHostHandler = new NameVirtualHostHandler();
    private final AccessLogCompletionListener accessLogCompletionListener = new AccessLogCompletionListener();
    private final StatsdClient statsdClient = new StatsdClient();
    private final StatsdCompletionListener statsdCompletionListener = new StatsdCompletionListener(statsdClient);
    private final RootHandler rootHandler = new RootHandler(nameVirtualHostHandler, accessLogCompletionListener, statsdCompletionListener);
    private final ApplicationContext context = mock(ApplicationContext.class);
    private final ExternalData externalData = mock(ExternalData.class);
    private final String virtualhost = "test.com";

    @Before
    public void setUp() {
        when(externalData.exist(anyString())).thenReturn(true);
        when(externalData.node(anyString())).thenAnswer(invocationOnMock -> {
            String key = invocationOnMock.getArgumentAt(0, String.class);
            EtcdNode node = new EtcdNode();

            switch (key) {
                case "": {
                    return node;
                }
                default: {
                    return EtcdGenericNode.NULL.get();
                }
            }
        });
        when(context.getBean(anyString())).thenReturn(new PoolHandler(context, externalData));
    }

    @Test
    public void testForceVirtualhostUpdate() {
        nameVirtualHostHandler.addHost(virtualhost, exchange -> {});
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(virtualhost));

        rootHandler.forceVirtualhostUpdate(virtualhost);
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(0));
    }

    @Test
    public void testForceAllUpdate() {
        nameVirtualHostHandler.addHost(virtualhost, exchange -> {});
        nameVirtualHostHandler.addHost(virtualhost + "1", exchange -> {});
        nameVirtualHostHandler.addHost(virtualhost + "2", exchange -> {});
        nameVirtualHostHandler.addHost(virtualhost + "3", exchange -> {});
        nameVirtualHostHandler.addHost(virtualhost + "4", exchange -> {});
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(5));

        rootHandler.forceAllUpdate();
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(0));
    }

    @Test
    public void testIgnoreForceVirtualhostUpdateIfPingHost() {
        String pingHost = "__ping__";

        nameVirtualHostHandler.addHost(pingHost, exchange -> {});
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(pingHost));

        rootHandler.forceVirtualhostUpdate(pingHost);
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(pingHost));
    }

    @Test
    public void testForcePoolUpdate() {
        PathGlobHandler pathGlobHandler = new PathGlobHandler();
        RuleTargetHandler ruleTargetHandler = mock(RuleTargetHandler.class);
        PoolHandler poolHandler = mock(PoolHandler.class);
        String poolName = "pool0";
        when(poolHandler.getPoolname()).thenReturn(poolName);
        when(ruleTargetHandler.getNext()).thenReturn(pathGlobHandler);
        pathGlobHandler.addPath("/", 0, poolHandler);

        nameVirtualHostHandler.addHost(virtualhost, ruleTargetHandler);
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(virtualhost));

        rootHandler.forcePoolUpdate(poolName);
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(0));
    }

    @Test
    public void testForcePoolUpdateByIpAclHandler() {
        PathGlobHandler pathGlobHandler = new PathGlobHandler();
        IPAddressAccessControlHandler ipAddressAccessControlHandler = new IPAddressAccessControlHandler().setNext(pathGlobHandler);
        RuleTargetHandler ruleTargetHandler = mock(RuleTargetHandler.class);
        PoolHandler poolHandler = mock(PoolHandler.class);
        pathGlobHandler.addPath("/", 0, poolHandler);
        String poolName = "pool0";
        when(poolHandler.getPoolname()).thenReturn(poolName);
        when(ruleTargetHandler.getNext()).thenReturn(ipAddressAccessControlHandler);

        nameVirtualHostHandler.addHost(virtualhost, ruleTargetHandler);
        assertThat(nameVirtualHostHandler.getHosts(), hasKey(virtualhost));

        rootHandler.forcePoolUpdate(poolName);
        assertThat(nameVirtualHostHandler.getHosts().size(), equalTo(0));
    }
}
