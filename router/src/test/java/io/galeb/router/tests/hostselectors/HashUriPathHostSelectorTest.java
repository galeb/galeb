package io.galeb.router.tests.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.galeb.router.client.hostselectors.HashUriPathHostSelector;
import io.undertow.server.HttpServerExchange;
import org.junit.Test;

import java.util.UUID;

public class HashUriPathHostSelectorTest extends AbstractHashHostSelectorTest {

    private final HashUriPathHostSelector hashUriPathHostSelector = new HashUriPathHostSelector();

    @Test
    public void testSelectHost() throws Exception {
        double errorPercentMax = 10.0;
        double limitOfNotHitsPercent = 5.0;
        int numPopulation = 20;
        doRandomTest(errorPercentMax, limitOfNotHitsPercent, numPopulation, hashUriPathHostSelector);
    }

    @Override
    int getResult(HttpServerExchange exchange, ExtendedLoadBalancingProxyClient.Host[] newHosts) {
        return hashUriPathHostSelector.selectHost(newHosts, exchange);
    }

    @Override
    void changeExchange(HttpServerExchange exchange, int x) {
        exchange.setRelativePath("/" + UUID.randomUUID().toString());
    }

}
