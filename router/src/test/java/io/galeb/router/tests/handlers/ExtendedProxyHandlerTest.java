package io.galeb.router.tests.handlers;

import io.galeb.router.handlers.ExtendedProxyHandler;
import io.undertow.server.HttpServerExchange;
import org.junit.Test;

public class ExtendedProxyHandlerTest {

    private final ExtendedProxyHandler proxyHandler = new ExtendedProxyHandler();

    @Test(expected = NullPointerException.class)
    public void testWhenInternalProxyHandlerIsNull() throws Exception {
        HttpServerExchange exchange = new HttpServerExchange(null);
        proxyHandler.handleRequest(exchange);
    }
}
