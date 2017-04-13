/**
 *
 */

package io.galeb.router.services;

import io.galeb.router.handlers.RootHandler;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xnio.Options;

import javax.annotation.PostConstruct;

@Service
public class RouterService {

    private final Logger      logger = LoggerFactory.getLogger(this.getClass());
    private final HttpHandler rootHandler;
    private final int routerPort = 8000; // TODO: property

    @Autowired
    public RouterService(final RootHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    @PostConstruct
    public void run() {
        logger.info(this.getClass().getSimpleName() + " started");

        final Undertow undertow = Undertow.builder().addHttpListener(routerPort, "0.0.0.0", rootHandler)
                .setIoThreads(4) // TODO: property
                .setWorkerThreads(4 * 8) // TODO: property
                .setBufferSize(16384) // TODO: property
                .setDirectBuffers(true) // TODO: property
                .setSocketOption(Options.BACKLOG, 65535) // TODO: property
                .setSocketOption(Options.KEEP_ALIVE, true)
                .setSocketOption(Options.REUSE_ADDRESSES, true)
                .setSocketOption(Options.TCP_NODELAY, true)
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setServerOption(UndertowOptions.ENABLE_STATISTICS, true)
                .build();
        undertow.start();
    }
}
