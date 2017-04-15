package io.galeb.router.tests.client;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertThat;

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
