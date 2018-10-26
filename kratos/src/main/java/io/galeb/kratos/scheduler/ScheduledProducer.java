package io.galeb.kratos.scheduler;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

import io.galeb.core.entity.Pool;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.kratos.repository.EnvironmentRepository;
import io.galeb.kratos.repository.TargetRepository;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;
import javax.jms.JMSException;
import javax.jms.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class ScheduledProducer {

    private static final int    PAGE_SIZE                 = 100;
    private static final String QUEUE_GALEB_HEALTH_PREFIX = SystemEnv.QUEUE_NAME.getValue();

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
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void sendToTargetsToQueue() {
        final String schedId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        final AtomicInteger counter = new AtomicInteger(0);
        environmentRepository.findAll().forEach(env -> {
            String environmentName = env.getName().replaceAll("[ ]+", "_").toLowerCase();
            long environmentId = env.getId();
            int page = 0;
            final Page<Target> targetsPage = sendTargets(counter, environmentName, environmentId, page);
            long size = targetsPage.getTotalElements();
            for (page = 1; page <= size/PAGE_SIZE; page++) {
                sendTargets(counter, environmentName, environmentId, page);
            }
            JsonEventToLogger eventToLogger = new JsonEventToLogger(this.getClass());
            eventToLogger.put("queue", QUEUE_GALEB_HEALTH_PREFIX + "_" + environmentId);
            eventToLogger.put("schedId", schedId);
            eventToLogger.put("environmentId", environmentId);
            eventToLogger.put("environmentName", environmentName);
            eventToLogger.put("countTarget", counter.get());
            eventToLogger.put("totalTargets", size);
            eventToLogger.put("time", System.currentTimeMillis() - start);
            eventToLogger.sendInfo();

            counter.set(0);
        });
    }

    private Page<Target> sendTargets(AtomicInteger counter, String environmentName, long environmentId, int page) {
        Page<Target> targetsPage = targetRepository.findByEnvironmentName(environmentName, new PageRequest(page, PAGE_SIZE));
        StreamSupport.stream(targetsPage.spliterator(), false).forEach(target -> {
            sendToQueue(target, environmentId, counter);
        });
        return targetsPage;
    }

    private void loggerEvent(Target target, Object e, String uniqueId, Long envId, String correlation) {
        JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("queue", QUEUE_GALEB_HEALTH_PREFIX + "_" + envId);
        event.put("target", target.getName());
        Pool pool = target.getPool();
        event.put("pool", pool.getName());
        if (correlation != null) {
            event.put("correlation", correlation);
        }
        if (uniqueId != null) {
            event.put("jmsMessageId", uniqueId);
        }
        if (e instanceof Exception) {
            event.sendError((Exception) e);
        } else {
            event.sendInfo();
        }
    }

    private void sendToQueue(final Target target, long envId, final AtomicInteger counter) {
        MessageCreator messageCreator = session -> {
            try {
                counter.incrementAndGet();
                final TargetDTO targetDTO = new TargetDTO(target);
                Message message = session.createObjectMessage(targetDTO);
                String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" +
                    (System.currentTimeMillis() / 10000L);
                defineUniqueId(message, uniqueId);
                loggerEvent(target, null, uniqueId, envId, targetDTO.getCorrelation());
                return message;
            } catch (Exception e) {
                loggerEvent(target, e, null, null, null);
            }
            return null;
        };
        try {
            template.send(QUEUE_GALEB_HEALTH_PREFIX + "_" + envId, messageCreator);
        } catch (Exception e) {
            loggerEvent(target, e, null, null, null);
        }
    }

    private void defineUniqueId(final Message message, String uniqueId) throws JMSException {
        message.setStringProperty("_HQ_DUPL_ID", uniqueId);
        message.setJMSMessageID(uniqueId);
        message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);
    }
}
