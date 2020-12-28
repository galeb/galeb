package io.galeb.router.tests.mocks;

import java.net.InetSocketAddress;

import io.undertow.server.Connectors;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

public class MockHttpServerExchange {
    public static HttpServerExchange createMockExchange(String url, final DefaultByteBufferPool bufferPool) {
        MockServerConnection connection = new MockServerConnection(bufferPool);
        HttpServerExchange exchange = new HttpServerExchange(connection, getRequestHeaders(),
            new HeaderMap(), 0);
        exchange.setSourceAddress(new InetSocketAddress("1.2.3.4", 44444));
        exchange.setRequestMethod(HttpString.tryFromString("GET"));
        exchange.setRelativePath(url);
        exchange.setProtocol(HttpString.tryFromString("HTTP"));
        exchange.setStatusCode(200);
        Connectors.setRequestStartTime(exchange);

        return exchange;
    }

    public static HttpServerExchange createMockExchange(String url) {
        final DefaultByteBufferPool bufferPool = new DefaultByteBufferPool(false, 1024, 0, 0);
        return createMockExchange(url, bufferPool);
    }

    private static HeaderMap getRequestHeaders() {
        HeaderMap headerMap = new HeaderMap();
        headerMap.add(HttpString.tryFromString("HOST"), "vhost.host.virtual");
        return headerMap;
    }
}
