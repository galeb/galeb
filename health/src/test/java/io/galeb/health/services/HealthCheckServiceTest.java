package io.galeb.health.services;

import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.JMSException;
import javax.jms.Message;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HealthCheckServiceTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @ClassRule
    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeClass
    public static void setEnvironmentVariables() {
        environmentVariables.set("ENVIRONMENT_NAME", "env1");
        environmentVariables.set("ZONE_ID", "zone1");
    }

    @Test
    public void shouldCheckTargetWithHealthy() throws JMSException {
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
            Message message = session.createObjectMessage(target);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

            return message;
        };
        //Action
        jmsTemplate.send(SystemEnv.QUEUE_NAME.getValue() + "_env1", messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive( "health-callback");
        HealthStatus hs = message.getBody(HealthStatus.class);

        //Assert
        Assert.assertTrue(hs.getStatus().equals(HealthStatus.Status.HEALTHY));
    }

    @Test
    public void shouldCheckTargetWithUnknowError() throws JMSException {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(1L);
            Target target = new Target();
            target.setName("http://127.0.0.1:1");
            target.setId(1L);
            target.setLastModifiedAt(new Date());
            target.setPool(pool);
            Message message = session.createObjectMessage(target);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

            return message;
        };

        //Action
        jmsTemplate.send(SystemEnv.QUEUE_NAME.getValue() + "_env1", messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive( "health-callback");
        HealthStatus hs = message.getBody(HealthStatus.class);

        //Assert
        Assert.assertTrue(hs.getStatus().equals(HealthStatus.Status.UNKNOWN));
    }

    @Test
    public void shouldCheckTargetWithFailStatusCode() throws JMSException {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(1L);
            pool.setHcHttpStatusCode("500");
            Target target = new Target();
            target.setName("http://127.0.0.1:" + SystemEnv.HEALTH_PORT.getValue());
            target.setId(1L);
            target.setLastModifiedAt(new Date());
            target.setPool(pool);
            Message message = session.createObjectMessage(target);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);
            return message;
        };

        //Action
        jmsTemplate.send(SystemEnv.QUEUE_NAME.getValue() + "_env1", messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive( "health-callback");
        HealthStatus hs = message.getBody(HealthStatus.class);

        //Assert
        Assert.assertTrue(hs.getStatus().equals(HealthStatus.Status.FAIL));
    }

    @Test
    public void shouldCheckTargetWithFailBody() throws JMSException {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(1L);
            pool.setHcBody("UNKNOW");
            Target target = new Target();
            target.setName("http://127.0.0.1:" + SystemEnv.HEALTH_PORT.getValue());
            target.setId(1L);
            target.setLastModifiedAt(new Date());
            target.setPool(pool);
            Message message = session.createObjectMessage(target);
            String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);
            return message;
        };

        //Action
        jmsTemplate.send(SystemEnv.QUEUE_NAME.getValue() + "_env1", messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive( "health-callback");
        HealthStatus hs = message.getBody(HealthStatus.class);

        //Assert
        Assert.assertTrue(hs.getStatus().equals(HealthStatus.Status.FAIL));
    }

    @Test
    public void shouldCheckTargetWithSameLastReason() {
        //Arrange
        MessageCreator messageCreator = session -> {
            Pool pool = new Pool();
            pool.setName("pool");
            pool.setId(2L);
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
        jmsTemplate.send(SystemEnv.QUEUE_NAME.getValue() + "_env1", messageCreator);
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive( "health-callback");

        //Assert
        Assert.assertTrue(message == null);
    }

}
