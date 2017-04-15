package io.galeb.router.tests.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.galeb.router.client.hostselectors.HashSourceIpHostSelector;
import io.undertow.server.HttpServerExchange;
import org.junit.Test;

import java.net.InetSocketAddress;

public class HashSourceIpHostSelectorTest extends AbstractHashHostSelectorTest {

    private final HashSourceIpHostSelector hashSourceIpHostSelector = new HashSourceIpHostSelector();

    @Test
    public void testSelectHost() throws Exception {
        double errorPercentMax = 1.0;
        double limitOfNotHitsPercent = 5.0;
        int numPopulation = 1000;
        doRandomTest(errorPercentMax, limitOfNotHitsPercent, numPopulation, hashSourceIpHostSelector);
    }

    @Override
    int getResult(HttpServerExchange exchange, ExtendedLoadBalancingProxyClient.Host[] newHosts) {
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
