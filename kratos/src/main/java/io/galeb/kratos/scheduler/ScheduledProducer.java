package io.galeb.kratos.scheduler;

import io.galeb.core.entity.Target;
import io.galeb.kratos.repository.EnvironmentRepository;
import io.galeb.kratos.repository.TargetRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

@Service
@EnableScheduling
public class ScheduledProducer {

    private static final Log LOGGER = LogFactory.getLog(ScheduledProducer.class);

    private static final int    PAGE_SIZE                 = 100;
    private static final String QUEUE_GALEB_HEALTH_PREFIX = "galeb-health";

    private final TargetRepository targetRepository;
    private final EnvironmentRepository environmentRepository;
    private final JmsTemplate template;

    @Autowired
    public ScheduledProducer(TargetRepository targetRepository, EnvironmentRepository environmentRepository, JmsTemplate template) {
        this.targetRepository = targetRepository;
        this.environmentRepository = environmentRepository;
        this.template = template;
    }

    @Scheduled(fixedDelay = 10000L)
    public void sendToTargetsToQueue() {
        final String schedId = UUID.randomUUID().toString();
        LOGGER.info("[sch " + schedId + "] Sending targets to queue " + QUEUE_GALEB_HEALTH_PREFIX);
        long start = System.currentTimeMillis();
        final AtomicInteger counter = new AtomicInteger(0);
        final long size = targetRepository.count();
        environmentRepository.findAll().stream().map(environment -> environment.getName().replaceAll("[ ]+", "_")).forEach(env -> {
            for (int page = 0; page <= size/PAGE_SIZE; page++) {
                Page<Target> targetsPage = targetRepository.findByEnvironmentName(env, new PageRequest(page, PAGE_SIZE));
                try {
                    StreamSupport.stream(targetsPage.spliterator(), false).forEach(target -> sendToQueue(target, env, counter));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    break;
                }
            }
            LOGGER.info("[sch " + schedId + "] Sent " + counter.get() + " targets to queue " + QUEUE_GALEB_HEALTH_PREFIX + "_" + env + " " +
                    "[" + (System.currentTimeMillis() - start) + " ms] (before to start this task: " + size + " targets from db)");

        });
    }

    private void sendToQueue(final Target target, String env, final AtomicInteger counter) {
        try {
            MessageCreator messageCreator = session -> {
                counter.incrementAndGet();
                Message message = session.createObjectMessage(target);
                String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
                defineUniqueId(message, uniqueId);

                if (LOGGER.isDebugEnabled()) LOGGER.debug("JMSMessageID: " + uniqueId + " - Target " + target.getName());
                return message;
            };
            template.send(QUEUE_GALEB_HEALTH_PREFIX + "_" + env, messageCreator);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void defineUniqueId(final Message message, String uniqueId) throws JMSException {
        message.setStringProperty("_HQ_DUPL_ID", uniqueId);
        message.setJMSMessageID(uniqueId);
        message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);
    }
}
