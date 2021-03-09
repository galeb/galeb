package io.galeb.kratos.schedule;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.core.enums.SystemEnv;
import io.galeb.kratos.repository.EnvironmentRepository;
import io.galeb.kratos.repository.TargetRepository;
import io.galeb.kratos.scheduler.ScheduledProducer;
import io.galeb.kratos.services.HealthSchema;
import io.galeb.kratos.services.HealthService;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "app.scheduling.enabled=false")
@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ScheduleProducerTest {
	
	private static final String QUEUE_NAME = SystemEnv.QUEUE_NAME.getValue();
	private static final String QUEUE_NAME_SEPARATOR = SystemEnv.QUEUE_NAME_SEPARATOR.getValue();
    
    @Autowired
    private JmsTemplate jmsTemplate;

    @Mock
    private HealthService healthService;

    private ScheduledProducer scheduledProducer;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private TargetRepository targetRepository;
    
    @Before
    public void setupScheduleProducer() {
        scheduledProducer = new ScheduledProducer(targetRepository, environmentRepository, jmsTemplate, healthService);
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

        Pool pool = new Pool();
        pool.setId(1L);
        pool.setName("pool");
        Target target = new Target();
        target.setName("http://127.0.0.1:8080");
        target.setId(1L);
        target.setLastModifiedAt(new Date());
        target.setPool(pool);
        List<Target> targets = new ArrayList<>();
        targets.add(target);
        Pageable pageable = new PageRequest(0, 100);
        Page<Target> page = new PageImpl<Target>(targets, pageable, 1);
        when(targetRepository.findByEnvironmentName("env1", pageable)).thenReturn(page);

        String sourceName = "xxx";
        String envId = String.valueOf(environment.getId());

        Set<HealthSchema.Source> sources = Collections.singleton(new HealthSchema.Source(sourceName, Collections.emptySet()));
        Set<HealthSchema.Env> envs = Collections.singleton(new HealthSchema.Env(envId, sources));
        when(healthService.get(anyString())).thenReturn(envs);

        //Action
        scheduledProducer.sendToTargetsToQueue();

        String queueName = QUEUE_NAME + QUEUE_NAME_SEPARATOR + envId + QUEUE_NAME_SEPARATOR + sourceName;
        jmsTemplate.setReceiveTimeout(5000);
        Message message = jmsTemplate.receive(queueName);

        //Assert
        Assert.assertTrue(message.isBodyAssignableTo(TargetDTO.class));
        TargetDTO t = message.getBody(TargetDTO.class);
        Assert.assertTrue(t.getTarget().getName().equals("http://127.0.0.1:8080"));

    }

}
