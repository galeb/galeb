package io.galeb.router.tests.backend;

import io.galeb.router.tests.client.HttpClient;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class SimulatedBackendService {

    @SuppressWarnings("unused")
    public enum ResponseBehavior {
        FASTTER(ResponseCodeHandler.HANDLE_200),
        FAST(exchange -> exchange.getResponseSender().send("A")),
        SLOW(exchange -> { Thread.sleep(5000); ResponseCodeHandler.HANDLE_200.handleRequest(exchange);}),
        HUGE(exchange -> {
            final byte[] bytes = "A".getBytes();
            int count = 1024 * 1024 * 100; // 100Mb
            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length * count);
            while (byteBuffer.hasRemaining()) byteBuffer.put(bytes);
            exchange.getResponseSender().send(byteBuffer);
        });

        private final HttpHandler handler;
        HttpHandler getHandler() {
            return handler;
        }

        ResponseBehavior(HttpHandler handler) {
            this.handler = handler;
        }
    }

    private final Log logger = LogFactory.getLog(this.getClass());
    private final HttpClient client;

    private Undertow undertow;

    @Autowired
    public SimulatedBackendService(final HttpClient client) {
        this.client = client;
    }

    public SimulatedBackendService setResponseBehavior(ResponseBehavior behavior) {
        int backendPort = 8080;
        this.undertow = Undertow.builder().addHttpListener(backendPort, "0.0.0.0", behavior.getHandler()).build();
        return this;
    }

    public void start() {
        try {
            client.get("http://127.0.0.1:" + 8080 + "/");
        } catch (Exception ignore) {
            undertow.start();
            logger.info(this.getClass().getSimpleName() + " started");
        }
    }

    public void stop() {
        undertow.stop();
        logger.info(this.getClass().getSimpleName() + " stopped");
    }
}
