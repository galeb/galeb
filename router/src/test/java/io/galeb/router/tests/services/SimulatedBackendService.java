package io.galeb.router.tests.services;

import io.undertow.Undertow;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class SimulatedBackendService {

    private final Log logger = LogFactory.getLog(this.getClass());

    private Undertow undertow;

    public SimulatedBackendService setBackendPort(int backendPort) {
        logger.info(this.getClass().getSimpleName() + ": using " + backendPort + "/tcp port");
        undertow = Undertow.builder().addHttpListener(backendPort, "0.0.0.0", ResponseCodeHandler.HANDLE_200).build();
        return this;
    }

    public void start() {
        if (undertow != null) {
            logger.info(this.getClass().getSimpleName() + " started");
            undertow.start();
        }
    }

}
