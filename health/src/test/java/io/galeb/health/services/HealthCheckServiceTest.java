package io.galeb.health.services;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Cookie;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.NottableString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.junit4.SpringRunner;

import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.core.enums.SystemEnv;
import io.galeb.health.util.CallBackQueue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HealthCheckServiceTest {

    @ClassRule
    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    public static ClientAndServer mockServer;
    private static EmbeddedActiveMQ embeddedBus;

    @Autowired
    private JmsTemplate jmsTemplate;

    public final String queueName = SystemEnv.QUEUE_NAME.getValue() + SystemEnv.QUEUE_NAME_SEPARATOR.getValue()
            + SystemEnv.ENVIRONMENT_ID.getValue() + SystemEnv.QUEUE_NAME_SEPARATOR.getValue()
            + SystemEnv.ZONE_ID.getValue().toLowerCase();
    
    @BeforeClass
    public static void setEnvironmentVariables() throws Exception {
        environmentVariables.set(SystemEnv.ENVIRONMENT_ID.name(), "env1");
        environmentVariables.set(SystemEnv.ZONE_ID.name(), "zone1");
        
        mockServer = ClientAndServer.startClientAndServer(5000);
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
        
        embeddedBus = new EmbeddedActiveMQ();
        embeddedBus.setConfigResourcePath("broker.xml");
        ActiveMQSecurityManager activeMQSecurityManager = new ActiveMQSecurityManager() {
            public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {return true;}
            public boolean validateUser(String user, String password) {return true;}
        };
        embeddedBus.setSecurityManager(activeMQSecurityManager);
        embeddedBus.start();
    }
    
    @AfterClass
    public static void freeResources() throws Exception {
        embeddedBus.stop();
        mockServer.stop();
        environmentVariables.clear(SystemEnv.ENVIRONMENT_ID.name(),SystemEnv.ZONE_ID.name());
    }

    @Test
    public void testCheckWithCookie() {
        Pool pool = new Pool();
        pool.setName("pool");
        pool.setId(1L);
        pool.setHcPath("/");
        pool.setHcHttpStatusCode("200");
        pool.setHcTcpOnly(false);
        Target target = new Target();
        target.setName("http://127.0.0.1:5000");
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        target.setPool(pool);
        
        TargetDTO targetDTO = new TargetDTO(target);
        
        CallBackQueue callBackQueue = Mockito.mock(CallBackQueue.class);
        Mockito.doNothing().when(callBackQueue).update(targetDTO);
        HealthCheckerService healthCheckerService = new HealthCheckerService(callBackQueue);
        
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
        
        Assert.assertEquals(0, requests[0].getCookies().size());
        Assert.assertEquals(0, requests[1].getCookies().size());
        Assert.assertEquals(0, requests[2].getCookies().size());
    }
    
    @Test
    public void shouldCheckTargetWithHealthy() throws JMSException, InterruptedException {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(1L);
            Target target = new Target();
            target.setName("http://127.0.0.1:" + SystemEnv.HEALTH_PORT.getValue());
            target.setId(1L);
            target.setLastModifiedAt(new Date());
            target.setPool(pool);
            TargetDTO tdto = new TargetDTO(target);
            Message message = session.createObjectMessage(tdto);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

            return message;
        };
        
        //Action
        jmsTemplate.send(queueName, messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive("health-callback");
        TargetDTO targetDTO = message.getBody(TargetDTO.class);

        //Assert
        HealthStatus healthStatus = targetDTO.getHealthStatus(SystemEnv.ZONE_ID.getValue().toLowerCase()).get();
        Assert.assertEquals(healthStatus.getStatus() ,HealthStatus.Status.HEALTHY);
    }

    @Test
    public void shouldCheckTargetWithServerUnreacheableReturnFail() throws JMSException {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(1L);
            pool.setHcTcpOnly(false);
            Target target = new Target();
            target.setName("http://127.0.0.1:1");
            target.setId(1L);
            target.setLastModifiedAt(new Date());
            target.setPool(pool);
            TargetDTO tdto = new TargetDTO(target);
            Message message = session.createObjectMessage(tdto);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

            return message;
        };

        //Action
        jmsTemplate.send(queueName, messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive("health-callback");
        TargetDTO targetDTO = message.getBody(TargetDTO.class);

        //Assert
        HealthStatus healthStatus = targetDTO.getHealthStatus(SystemEnv.ZONE_ID.getValue().toLowerCase()).get();
        Assert.assertEquals(HealthStatus.Status.FAIL, healthStatus.getStatus());
    }

    @Test
    public void shouldCheckTargetWithFailStatusCode() throws JMSException {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(1L);
            pool.setHcHttpStatusCode("500");
            pool.setHcTcpOnly(false);
            Target target = new Target();
            target.setName("http://127.0.0.1:" + SystemEnv.HEALTH_PORT.getValue());
            target.setId(1L);
            target.setLastModifiedAt(new Date());
            target.setPool(pool);
            TargetDTO tdto = new TargetDTO(target);
            Message message = session.createObjectMessage(tdto);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);
            return message;
        };

        //Action
        jmsTemplate.send(queueName, messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive("health-callback");
        TargetDTO targetDTO = message.getBody(TargetDTO.class);

        //Assert
        HealthStatus healthStatus = targetDTO.getHealthStatus(SystemEnv.ZONE_ID.getValue().toLowerCase()).get();
        Assert.assertEquals(HealthStatus.Status.FAIL, healthStatus.getStatus());
    }

    @Test
    public void shouldCheckTargetWithFailBody() throws JMSException {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(1L);
            pool.setHcTcpOnly(false);
            pool.setHcBody("UNKNOWN");
            Target target = new Target();
            target.setName("http://127.0.0.1:" + SystemEnv.HEALTH_PORT.getValue());
            target.setId(1L);
            target.setLastModifiedAt(new Date());
            target.setPool(pool);
            TargetDTO tdto = new TargetDTO(target);
            Message message = session.createObjectMessage(tdto);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);
            return message;
        };

        //Action
        jmsTemplate.send(queueName, messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive( "health-callback");
        TargetDTO targetDTO = message.getBody(TargetDTO.class);

        //Assert
        HealthStatus healthStatus = targetDTO.getHealthStatus(SystemEnv.ZONE_ID.getValue().toLowerCase()).get();
        Assert.assertEquals(healthStatus.getStatus(), HealthStatus.Status.FAIL);
    }

    @Test
    public void shouldCheckTargetWithSameLastReason() {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(2L);
            pool.setHcTcpOnly(false);
            Target target = new Target();
            target.setName("http://127.0.0.1:" + SystemEnv.HEALTH_PORT.getValue());
            target.setId(2L);
            target.setLastModifiedAt(new Date());
            target.setPool(pool);
            HealthStatus healthStatus = new HealthStatus();
            healthStatus.setStatusDetailed("HEALTHY");
            healthStatus.setStatus(HealthStatus.Status.HEALTHY);
            healthStatus.setSource("zone1");
            Set<HealthStatus> healthStatuses = new HashSet<>();
            healthStatuses.add(healthStatus);
            target.setHealthStatus(healthStatuses);
            Message message = session.createObjectMessage(target);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

            return message;
        };

        //Action
        jmsTemplate.send(queueName, messageCreator);
        jmsTemplate.setReceiveTimeout(500);
        Message message = jmsTemplate.receive( "health-callback");

        //Assert
        Assert.assertNull(message);
    }


}
