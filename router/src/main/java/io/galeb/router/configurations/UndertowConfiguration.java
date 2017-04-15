package io.galeb.router.configurations;

import io.galeb.router.SystemEnvs;
import io.galeb.router.handlers.RootHandler;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xnio.Options;

@Configuration
public class UndertowConfiguration {

    private final int port = Integer.parseInt(SystemEnvs.ROUTER_PORT.getValue());
    private final RootHandler rootHandler;

    @Autowired
    public UndertowConfiguration(final RootHandler rootHandler) {
        this.rootHandler = rootHandler;
    }

    @Bean
    public Undertow undertow() {
        return Undertow.builder().addHttpListener(port, "0.0.0.0", rootHandler)
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
    }
}
