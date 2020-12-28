package io.galeb.router.tests.handlers;

import static io.galeb.router.tests.mocks.MockHttpServerExchange.createMockExchange;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.galeb.router.handlers.InfoHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class InfoHandlerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void canGetServerInfo() {
        HttpServerExchange exchange = createMockExchange("/");
        HttpHandler handler = new InfoHandler();

        try {
            handler.handleRequest(exchange);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }

        Assert.assertEquals("", exchange.getResponseHeaders().get(Headers.CONTENT_TYPE).get(0), "text/plain");
        Assert.assertEquals("", exchange.getResponseHeaders().get(Headers.SERVER).get(0), "GALEB");
        Assert.assertEquals("", exchange.getStatusCode(), 200);
    }
}
