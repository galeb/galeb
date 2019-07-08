package io.galeb.health.services;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Cookie;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.NottableString;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.health.util.CallBackQueue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HealthCheckServiceUnitTest {
    private static Integer BACKEND_PORT = 5000;
    
    private static ClientAndServer mockServer;

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
    }
    
    @AfterClass
    public static void cleanup() {
        if (mockServer.isRunning()) {
            mockServer.stop();
        }
    }

    @Test
    public void testCheckWithCookie() {
        Pool pool = new Pool();
        pool.setName("pool");
        pool.setId(1L);
        pool.setHcPath("/");
        Target target = new Target();
        target.setName("http://127.0.0.1:" + BACKEND_PORT.toString());
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        target.setPool(pool);
        
        TargetDTO targetDTO = new TargetDTO(target);
        
        HealthCheckerService healthCheckerService = new HealthCheckerService(new CallBackQueue(null));
        
        healthCheckerService.check(targetDTO);
        
        mockServer.verify(HttpRequest.request()
                .withMethod("GET")
                .withPath("/")
                .withHeader("user-agent", "Galeb_HealthChecker/1.0"));
        
        healthCheckerService.check(targetDTO);

        mockServer.verify(HttpRequest.request()
                .withMethod("GET")
                .withPath("/")
                .withHeader("user-agent", "Galeb_HealthChecker/1.0")); 
       
        healthCheckerService.check(targetDTO);
        
        mockServer.verify(HttpRequest.request()
                .withMethod("GET")
                .withPath("/")
                .withHeader("user-agent", "Galeb_HealthChecker/1.0"));
        
        HttpRequest[] requests = mockServer.retrieveRecordedRequests(HttpRequest.request()
                .withMethod("GET")
                .withPath("/")
                .withHeader("user-agent", "Galeb_HealthChecker/1.0"));
        
        Assert.assertEquals(requests[0].getCookies().size(), 0);
        Assert.assertEquals(requests[1].getCookies().size(), 0);
        Assert.assertEquals(requests[2].getCookies().size(), 0);
    }
}
