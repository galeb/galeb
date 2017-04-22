package io.galeb.health.services;

import io.galeb.health.SystemEnvs;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import org.springframework.stereotype.Service;
import org.xnio.Options;

import javax.annotation.PostConstruct;

@Service
public class SimpleWebServerService {

    private final int port = Integer.parseInt(SystemEnvs.HEALTH_PORT.getValue());

    @PostConstruct
    public void init() {
        Undertow.builder().addHttpListener(port, "0.0.0.0", pingHandler())
                .setSocketOption(Options.KEEP_ALIVE, true)
                .setSocketOption(Options.REUSE_ADDRESSES, true)
                .setSocketOption(Options.TCP_NODELAY, true)
                .build().start();
    }

    private HttpHandler pingHandler() {
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().put(Headers.SERVER, "GALEB");
            exchange.getResponseSender().send("WORKING");
            exchange.endExchange();
        };
    }
}
