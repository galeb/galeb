package io.galeb.kratos.scheduler;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.kratos.repository.EnvironmentRepository;
import io.galeb.kratos.repository.TargetRepository;
import io.galeb.kratos.services.HealthSchema.Source;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import javax.jms.JMSException;
import javax.jms.Message;

import io.galeb.kratos.services.HealthSchema;
import io.galeb.kratos.services.HealthService;
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
    private static final String QUEUE_SEPARATOR           = "-";
    private static final String MESSAGE_ID_SEPARATOR      = "-";

    private final TargetRepository targetRepository;
    private final EnvironmentRepository environmentRepository;
    private final JmsTemplate template;
    private final HealthService healthService;

    @Autowired
    public ScheduledProducer(TargetRepository targetRepository, EnvironmentRepository environmentRepository, JmsTemplate template, HealthService healthService) {
        this.targetRepository = targetRepository;
        this.environmentRepository = environmentRepository;
        this.template = template;
        this.healthService = healthService;
    }

    @Scheduled(fixedDelay = 10000L)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void sendToTargetsToQueue() {
        final String schedId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        final AtomicInteger counter = new AtomicInteger(0);
        environmentRepository.findAll().stream().collect(shuffleEnvironments()).forEach(env -> {
            String environmentName = env.getName().replaceAll("[ ]+", "_").toLowerCase();
            long environmentId = env.getId();
            Long numTargets = targetRepository.countTargetsBy(env.getName());
            final LinkedList<Integer> pages = IntStream.rangeClosed(0, (int) (numTargets / PAGE_SIZE)).boxed()
                .collect(shufflePages());

            while (!pages.isEmpty()) {
                int page = pages.removeLast();
                sendTargets(counter, environmentName, environmentId, page);
            }

            JsonEventToLogger eventToLogger = new JsonEventToLogger(this.getClass());
            eventToLogger.put("queuePrefix", QUEUE_GALEB_HEALTH_PREFIX + QUEUE_SEPARATOR + environmentId);
            eventToLogger.put("schedId", schedId);
            eventToLogger.put("message", "Sending all targets to environment queue");
            eventToLogger.put("environmentId", environmentId);
            eventToLogger.put("environmentName", environmentName);
            eventToLogger.put("sentTargets", counter.get());
            eventToLogger.put("readTargets", numTargets);
            eventToLogger.put("time", System.currentTimeMillis() - start);
            eventToLogger.sendInfo();

            counter.set(0);
        });
    }

    private Collector<Environment, ?, List<Environment>> shuffleEnvironments() {
        return Collectors.collectingAndThen(Collectors.toList(),
            collected -> {
                Collections.shuffle(collected);
                return collected;
            });
    }

    private Collector<Integer, ?, LinkedList<Integer>> shufflePages() {
        return Collectors.collectingAndThen(Collectors.toCollection(LinkedList::new),
            collected -> {
                Collections.shuffle(collected);
                return collected;
            });
    }

    private Page<Target> sendTargets(AtomicInteger counter, String environmentName, long environmentId, int page) {
        Page<Target> targetsPage = targetRepository.findByEnvironmentName(environmentName, new PageRequest(page, PAGE_SIZE));
        StreamSupport.stream(targetsPage.spliterator(), false).forEach(target -> {
            sendToQueue(target, environmentId, counter, page);
        });
        return targetsPage;
    }

    private void logException(Target target, Exception e) {
        logEvent(target, e, null, null, null);
    }

    private void logEvent(Target target, Object context, String uniqueId, String queue, String correlation) {
        JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("queue", queue);
        event.put("target", target.getName());
        event.put("message", "Sending target to queue");
        Pool pool = target.getPool();
        event.put("pool", pool.getName());
        if (correlation != null) {
            event.put("correlation", correlation);
        }
        if (uniqueId != null) {
            event.put("jmsMessageId", uniqueId);
        }
        if (context instanceof Integer) {
            event.put("context", String.valueOf(context));
        }
        if (context instanceof Exception) {
            event.sendError((Exception) context);
        } else {
            event.sendInfo();
        }
    }

    private void sendToQueue(final Target target, long envId, final AtomicInteger counter, Integer page) {
        // @formatter:off
        final String queuePrefix = QUEUE_GALEB_HEALTH_PREFIX + QUEUE_SEPARATOR + envId;
        final String uniqueIdPrefix = "ID:" + target.getId()               + MESSAGE_ID_SEPARATOR +
                                      target.getLastModifiedAt().getTime() + MESSAGE_ID_SEPARATOR +
                                      (System.currentTimeMillis() / 10000L);
        // @formatter:on

        try {
            Set<HealthSchema.Env> healthEnvs = healthService.get(String.valueOf(envId));
            for (HealthSchema.Env healthEnv : healthEnvs) {
                final Set<Source> sources = healthEnv.getSources();
                if (sources != null && !sources.isEmpty()) {
                    for (HealthSchema.Source source : sources) {
                        if (source != null) {
                            String sourceName = source.getName().toLowerCase();
                            String queue = queuePrefix + QUEUE_SEPARATOR + sourceName;
                            final TargetDTO targetDTO = new TargetDTO(target);
                            logEvent(target, page, uniqueIdPrefix, queue, targetDTO.getCorrelation());
                            template.send(queue, session -> {
                                try {
                                    counter.incrementAndGet();
                                    Message message = session.createObjectMessage(targetDTO);
                                    String uniqueId = uniqueIdPrefix + MESSAGE_ID_SEPARATOR + sourceName;
                                    defineUniqueId(message, uniqueId);
                                    return message;
                                } catch (Exception e) {
                                    logException(target, e);
                                }
                                return null;
                            });
                        } else {
                            JsonEventToLogger event = new JsonEventToLogger(this.getClass());
                            event.put("message", "Error sending target to queue. Source is null");
                            event.sendError();
                        }
                    }
                } else {
                    JsonEventToLogger event = new JsonEventToLogger(this.getClass());
                    event.put("message", "Error sending target to queue. Env.[]Source is null");
                    event.sendError();
                }
            }

        } catch (Exception e) {
            logException(target, e);
        }
    }

    private void defineUniqueId(final Message message, String uniqueId) throws JMSException {
        message.setStringProperty("_HQ_DUPL_ID", uniqueId);
        message.setJMSMessageID(uniqueId);
        message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);
    }
}
