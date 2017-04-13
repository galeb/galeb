/**
 *
 */

package io.galeb.router.services;

import io.galeb.router.configurations.SystemEnvs;
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

    private final int port = Integer.parseInt(SystemEnvs.ROUTER_PORT.getValue());

    private final Logger      logger = LoggerFactory.getLogger(this.getClass());
    private final HttpHandler rootHandler;

    @Autowired
    public RouterService(final RootHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    @PostConstruct
    public void run() {
        logger.info(this.getClass().getSimpleName() + " started");

        final Undertow undertow = Undertow.builder().addHttpListener(port, "0.0.0.0", rootHandler)
                .setIoThreads(Integer.parseInt(SystemEnvs.IO_THREADS.getValue()))
                .setWorkerThreads(Integer.parseInt(SystemEnvs.WORKER_THREADS.getValue()))
                .setBufferSize(Integer.parseInt(SystemEnvs.BUFFER_SIZE.getValue()))
                .setDirectBuffers(Boolean.parseBoolean(SystemEnvs.DIRECT_BUFFER.getValue()))
                .setSocketOption(Options.BACKLOG, Integer.parseInt(SystemEnvs.BACKLOG.getValue()))
                .setSocketOption(Options.KEEP_ALIVE, true)
                .setSocketOption(Options.REUSE_ADDRESSES, true)
                .setSocketOption(Options.TCP_NODELAY, true)
                .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, true)
                .setServerOption(UndertowOptions.ENABLE_STATISTICS, true)
                .build();
        undertow.start();
    }
}
