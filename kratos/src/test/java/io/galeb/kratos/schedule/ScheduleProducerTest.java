package io.galeb.kratos.schedule;

import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import io.galeb.kratos.repository.EnvironmentRepository;
import io.galeb.kratos.repository.PoolRepository;
import io.galeb.kratos.repository.TargetRepository;
import io.galeb.kratos.scheduler.ScheduledProducer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "app.scheduling.enabled=false")
@SpringBootTest
public class ScheduleProducerTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    private ScheduledProducer scheduledProducer;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private TargetRepository targetRepository;

    @Mock
    private PoolRepository poolRepository;

    @Before
    public void setupScheduleProducer() {
        scheduledProducer = new ScheduledProducer(targetRepository, environmentRepository, poolRepository, jmsTemplate);
    }

    @Test
    public void shouldSendTargetToQueue() throws JMSException {
        //Arrange
        Environment environment = new Environment();
        environment.setId(1L);
        environment.setName("env1");
        List<Environment> environments = new ArrayList<>();
        environments.add(environment);
        when(environmentRepository.findAll()).thenReturn(environments);


        Target target = new Target();
        target.setName("http://127.0.0.1:8080");
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        List<Target> targets = new ArrayList<>();
        targets.add(target);
        Pageable pageable = new PageRequest(0, 100);
        Page<Target> page = new PageImpl<Target>(targets, pageable, 1);
        when(targetRepository.findByEnvironmentName("env1", pageable)).thenReturn(page);

        Set<Pool> pools = new HashSet<>();
        Pool pool = new Pool();
        pool.setId(1L);
        pool.setName("pool");
        pools.add(pool);
        when(poolRepository.findAllByTargetId(target.getId())).thenReturn(pools);

        //Action
        scheduledProducer.sendToTargetsToQueue();
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive(SystemEnv.QUEUE_NAME.getValue() + "_" + "env1");

        //Assert
        Assert.assertTrue(message.isBodyAssignableTo(Target.class));
        Target t = message.getBody(Target.class);
        Assert.assertTrue(t.getName().equals("http://127.0.0.1:8080"));

    }

}
