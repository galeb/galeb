package io.galeb.health.services;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mockserver.MockServer;
import org.mockserver.model.Cookie;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.NottableString;
import org.springframework.http.HttpStatus;

import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.health.util.CallBackQueue;

public class HealthCheckServiceUnitTest {
    private static Integer BACKEND_PORT = 5000;
    private static Integer BACKEND_PORT_TCP_ONLY = 5001;
    private static Integer BACKEND_PORT_NOT_SERVER_NOT_RUN = 5002;
    
    private static ClientAndServer mockServer;
    private static ServerSocket serverSocket;
    private static Thread serverTCP;
    

    @ClassRule
    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    
    @BeforeClass
    public static void setupClass() {
        environmentVariables.set("ENVIRONMENT_NAME", "env1");
        environmentVariables.set("ZONE_ID", "zone1");
        
        mockServer = ClientAndServer.startClientAndServer(BACKEND_PORT);

        mockServer.when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/")
                        .withHeaders(
                                Header.header("user-agent", "Galeb_HealthChecker/1.0"),
                                Header.header(NottableString.not("cookie")))
                        .withKeepAlive(true))
                  .respond(HttpResponse.response()
                        .withCookie(new Cookie("session", "test-cookie"))
                        .withStatusCode(HttpStatus.OK.value()));
        
        mockTCPOnlyServer();
    }
    
    @AfterClass
    public static void cleanup() {
        if (mockServer.isRunning()) {
            mockServer.stop();
        }
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {}
        }
    }

    @Test
    public void testCheckTCPOnly() throws Exception {
        Pool pool = new Pool();
        pool.setName("pool");
        pool.setId(1L);
        pool.setHcTcpOnly(true);
        Target target = new Target();
        target.setName("http://127.0.0.1:" + BACKEND_PORT_TCP_ONLY.toString());
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        target.setPool(pool);

        TargetDTO targetDTO = new TargetDTO(target);

        CallBackQueue callBackQueue = Mockito.mock(CallBackQueue.class);
        Mockito.doNothing().when(callBackQueue).update(targetDTO);
        HealthCheckerService healthCheckerService = new HealthCheckerService(callBackQueue);

        healthCheckerService.check(targetDTO);
        
        Assert.assertEquals(HealthStatus.Status.HEALTHY, targetDTO.getHealthStatus("zone1").get().getStatus());
        Mockito.verify(callBackQueue, Mockito.times(1)).update(targetDTO);
    }

    @Test
    public void testCheckTCPOnlyDNS() throws Exception {
        Pool pool = new Pool();
        pool.setName("pool");
        pool.setId(1L);
        pool.setHcTcpOnly(true);
        Target target = new Target();
        target.setName("https://localhost");
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        target.setPool(pool);

        TargetDTO targetDTO = new TargetDTO(target);

        CallBackQueue callBackQueue = Mockito.mock(CallBackQueue.class);
        Mockito.doNothing().when(callBackQueue).update(targetDTO);
        HealthCheckerService healthCheckerService = new HealthCheckerService(callBackQueue);

        healthCheckerService.check(targetDTO);

        Assert.assertEquals(HealthStatus.Status.FAIL, targetDTO.getHealthStatus("zone1").get().getStatus());
        Assert.assertEquals("Fail to estabilished tcp connection", targetDTO.getHealthStatus("zone1").get().getStatusDetailed());
        Mockito.verify(callBackQueue, Mockito.times(1)).update(targetDTO);
    }

    @Test
    public void testCheckTCPOnlyDNSMalformedURL() throws Exception {
        Pool pool = new Pool();
        pool.setName("pool");
        pool.setId(1L);
        pool.setHcTcpOnly(true);
        Target target = new Target();
        target.setName("httpsw://localhost");
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        target.setPool(pool);

        TargetDTO targetDTO = new TargetDTO(target);

        CallBackQueue callBackQueue = Mockito.mock(CallBackQueue.class);
        Mockito.doNothing().when(callBackQueue).update(targetDTO);
        HealthCheckerService healthCheckerService = new HealthCheckerService(callBackQueue);

        healthCheckerService.check(targetDTO);

        Assert.assertEquals(HealthStatus.Status.FAIL, targetDTO.getHealthStatus("zone1").get().getStatus());
        Assert.assertEquals("Malformed url: httpsw://localhost", targetDTO.getHealthStatus("zone1").get().getStatusDetailed());
        Mockito.verify(callBackQueue, Mockito.times(1)).update(targetDTO);
    }

    @Test
    public void testCheckTCPOnlyNuloExecuteCheckHTTP() throws Exception {
        Pool pool = new Pool();
        pool.setName("pool");
        pool.setId(1L);
        pool.setHcPath("/");
        pool.setHcHttpStatusCode("200");
        pool.setHcTcpOnly(null);
        
        pool = Mockito.spy(pool);
        Mockito.when(pool.getHcTcpOnly()).thenReturn(null);
        
        Target target = new Target();
        target.setName("http://127.0.0.1:" + BACKEND_PORT.toString());
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        target.setPool(pool);

        TargetDTO targetDTO = new TargetDTO(target);
        
        CallBackQueue callBackQueue = Mockito.mock(CallBackQueue.class);
        Mockito.doNothing().when(callBackQueue).update(targetDTO);
        HealthCheckerService healthCheckerService = new HealthCheckerService(callBackQueue);

        healthCheckerService.check(targetDTO);      
        waitAsyncRequest("/", mockServer);
        
        mockServer.verify(HttpRequest.request()
                .withMethod("GET")
                .withPath("/")
                .withHeader("user-agent", "Galeb_HealthChecker/1.0")); 
        
        Assert.assertEquals(HealthStatus.Status.HEALTHY, targetDTO.getHealthStatus("zone1").get().getStatus());
        Mockito.verify(callBackQueue, Mockito.times(1)).update(targetDTO);
    }

    
    @Test
    public void testCheckTCPOnlyFailStatus() throws Exception {
        Pool pool = new Pool();
        pool.setName("pool");
        pool.setId(1L);
        pool.setHcTcpOnly(true);
        Target target = new Target();
        target.setName("http://127.0.0.1:" + BACKEND_PORT_NOT_SERVER_NOT_RUN.toString());
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        target.setPool(pool);

        TargetDTO targetDTO = new TargetDTO(target);

        CallBackQueue callBackQueue = Mockito.mock(CallBackQueue.class);
        Mockito.doNothing().when(callBackQueue).update(targetDTO);
        HealthCheckerService healthCheckerService = new HealthCheckerService(callBackQueue);

        healthCheckerService.check(targetDTO);
        
        Assert.assertEquals(HealthStatus.Status.FAIL, targetDTO.getHealthStatus("zone1").get().getStatus());
        Mockito.verify(callBackQueue, Mockito.times(1)).update(targetDTO);
    }
    
    private static void mockTCPOnlyServer() {
    	serverTCP = new Thread() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(BACKEND_PORT_TCP_ONLY);
                    while (true) {
                        serverSocket.accept();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        serverTCP.start();
    }
    
	private void waitAsyncRequest(String path, ClientAndServer mockServer) throws InterruptedException {
		HttpRequest[] asyncRequestsRetrieved = null;
        int start = 0;
        while (start < 10) {
            asyncRequestsRetrieved = mockServer.retrieveRecordedRequests(HttpRequest.request().withPath(path));
            if (asyncRequestsRetrieved != null && asyncRequestsRetrieved.length > 0) {
                return;
            }
            Thread.sleep(5L);
        }
        
        Assert.fail("Do not receive request in path: " + path);
	}
}
