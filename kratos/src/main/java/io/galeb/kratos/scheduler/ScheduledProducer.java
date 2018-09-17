package io.galeb.kratos.scheduler;

import com.google.gson.Gson;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import io.galeb.kratos.repository.EnvironmentRepository;
import io.galeb.kratos.repository.TargetRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

@Service
@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class ScheduledProducer {

    private static final Log LOGGER = LogFactory.getLog(ScheduledProducer.class);

    private static final int    PAGE_SIZE                 = 100;
    private static final String QUEUE_GALEB_HEALTH_PREFIX = SystemEnv.QUEUE_NAME.getValue();
    private static final String LOGGING_TAGS              = SystemEnv.LOGGING_TAGS.getValue();

    private final TargetRepository targetRepository;
    private final EnvironmentRepository environmentRepository;
    private final JmsTemplate template;

    private Gson gson = new Gson();

    @Autowired
    public ScheduledProducer(TargetRepository targetRepository, EnvironmentRepository environmentRepository, JmsTemplate template) {
        this.targetRepository = targetRepository;
        this.environmentRepository = environmentRepository;
        this.template = template;
    }

    @Scheduled(fixedDelay = 10000L)
    public void sendToTargetsToQueue() {
        final String schedId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        final AtomicInteger counter = new AtomicInteger(0);
        environmentRepository.findAll().stream().forEach(env -> {
            String environmentName = env.getName().replaceAll("[ ]+", "_").toLowerCase();
            long environmentId = env.getId();
            LOGGER.info("[sch " + schedId + "] Sending targets to queue " + QUEUE_GALEB_HEALTH_PREFIX + "_" + environmentId + " (" + environmentName + ")");
            Page<Target> targetsPage = targetRepository.findByEnvironmentName(environmentName, new PageRequest(0, PAGE_SIZE));
            StreamSupport.stream(targetsPage.spliterator(), false).forEach(target -> sendToQueue(target, environmentId, counter));
            long size = targetsPage.getTotalElements();
            for (int page = 1; page <= size/PAGE_SIZE; page++) {
                try {
                    targetsPage = targetRepository.findByEnvironmentName(environmentName, new PageRequest(page, PAGE_SIZE));
                    StreamSupport.stream(targetsPage.spliterator(), false).forEach(target -> sendToQueue(target, environmentId, counter));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    break;
                }
            }
            Map<String, String> mapLog = new HashMap<>();
            mapLog.put("class", ScheduledProducer.class.getSimpleName().toString());
            mapLog.put("queue", QUEUE_GALEB_HEALTH_PREFIX + "_" + environmentId);
            mapLog.put("schedId", schedId);
            mapLog.put("countTarget", String.valueOf(counter.get()));
            mapLog.put("time", String.valueOf(System.currentTimeMillis() - start));
            mapLog.put("tags", LOGGING_TAGS);

            LOGGER.info(gson.toJson(mapLog));
            counter.set(0);
        });
    }

    private void sendToQueue(final Target target, long envId, final AtomicInteger counter) {
        try {
            MessageCreator messageCreator = session -> {
                counter.incrementAndGet();
                Message message = session.createObjectMessage(target);
                String uniqueId = "ID:" + target.getId() + "-" + target.getLastModifiedAt().getTime() + "-" + (System.currentTimeMillis() / 10000L);
                defineUniqueId(message, uniqueId);

                if (LOGGER.isDebugEnabled()) LOGGER.debug("JMSMessageID: " + uniqueId + " - Target " + target.getName());
                return message;
            };
            template.send(QUEUE_GALEB_HEALTH_PREFIX + "_" + envId, messageCreator);
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
