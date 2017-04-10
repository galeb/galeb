package io.galeb.router.tests.services;

import io.undertow.Undertow;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.springframework.stereotype.Service;

@Service
public class SimulatedBackendService {

    private Undertow undertow;

    public SimulatedBackendService setBackendPort(int backendPort) {
        undertow = Undertow.builder().addHttpListener(backendPort, "0.0.0.0", ResponseCodeHandler.HANDLE_200).build();
        return this;
    }

    public void start() {
        if (undertow != null) {
            undertow.start();
        }
    }

}
