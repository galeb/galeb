/**
 *
 */

package io.galeb.router.services;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xnio.Options;

import javax.annotation.PostConstruct;

@Service
public class RouterService {

    private final Logger      logger = LoggerFactory.getLogger(this.getClass());
    private final HttpHandler rootHandler;

    @Autowired
    public RouterService(@Value("#{rootHandler}") final HttpHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    @PostConstruct
    public void run() {
        logger.info(this.getClass().getSimpleName() + " started");

        final Undertow undertow = Undertow.builder().addHttpListener(8000, "0.0.0.0", rootHandler)
                .setIoThreads(4)
                .setWorkerThreads(4 * 8)
                .setBufferSize(16384)
                .setDirectBuffers(true)
                .setSocketOption(Options.BACKLOG, 65535)
                .setSocketOption(Options.KEEP_ALIVE, true)
                .setSocketOption(Options.REUSE_ADDRESSES, true)
                .setSocketOption(Options.TCP_NODELAY, true)
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setServerOption(UndertowOptions.ENABLE_STATISTICS, true)
                .build();
        undertow.start();
    }
}
