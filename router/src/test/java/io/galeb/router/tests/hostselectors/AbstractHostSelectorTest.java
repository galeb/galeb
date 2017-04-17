package io.galeb.router.tests.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
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

}
