package io.galeb.kratos.queue;

import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Target;
import io.galeb.kratos.repository.HealthStatusRepository;
import io.galeb.kratos.repository.TargetRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CallbackConsumerTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private HealthStatusRepository healthStatusRepository;


    @Test
    public void shouldCreateNewHealthStatus() throws InterruptedException {
        //Arrange
        Target target = new Target();
        target.setName("http://127.0.0.1:8080");
        Target targetSaved = targetRepository.save(target);

        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setSource("source");
        healthStatus.setStatus(HealthStatus.Status.HEALTHY);
        healthStatus.setStatusDetailed("Detailed");
        healthStatus.setTarget(targetSaved);

        //Action
        jmsTemplate.convertAndSend("health-callback", healthStatus);
        Thread.sleep(5000); //TODO Need for await finish the queue. Are there other way for this?

        //Assert
        HealthStatus healthStatusUpdated = healthStatusRepository.findBySourceAndTargetId(healthStatus.getSource(), targetSaved.getId());
        Assert.assertTrue(healthStatusUpdated.getStatus().equals(HealthStatus.Status.HEALTHY));
    }

    @Test
    public void shouldCreateUpdateHealthStatus() throws InterruptedException {
        //Arrange
        Target target = new Target();
        target.setName("http://127.0.0.1:8081");
        Target targetSaved = targetRepository.save(target);

        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setSource("source");
        healthStatus.setStatus(HealthStatus.Status.HEALTHY);
        healthStatus.setStatusDetailed("Detailed");
        healthStatus.setTarget(targetSaved);
        healthStatusRepository.save(healthStatus);

        healthStatus.setStatus(HealthStatus.Status.FAIL);
        healthStatus.setStatusDetailed("Failed to connect");

        //Action
        jmsTemplate.convertAndSend("health-callback", healthStatus);
        Thread.sleep(5000); //TODO Need for await finish the queue. Are there other way for this?

        //Assert
        HealthStatus healthStatusUpdated = healthStatusRepository.findBySourceAndTargetId(healthStatus.getSource(), targetSaved.getId());
        Assert.assertTrue(healthStatusUpdated.getStatus().equals(HealthStatus.Status.FAIL));
        Assert.assertTrue(healthStatusUpdated.getStatusDetailed().equals("Failed to connect"));
    }

}
