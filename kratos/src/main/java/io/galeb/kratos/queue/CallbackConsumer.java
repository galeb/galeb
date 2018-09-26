package io.galeb.kratos.queue;

import com.google.gson.Gson;
import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import io.galeb.core.services.ChangesService;
import io.galeb.core.services.VersionService;
import io.galeb.kratos.repository.HealthStatusRepository;
import io.galeb.kratos.repository.TargetRepository;
import io.galeb.kratos.services.HealthService;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CallbackConsumer {

    private static final Log LOGGER = LogFactory.getLog(CallbackConsumer.class);

    private static final String QUEUE_HEALTH_CALLBACK = "health-callback";
    private static final String QUEUE_HEALTH_REGISTER = "health-register";

    private final HealthStatusRepository healthStatusRepository;
    private final TargetRepository targetRepository;

    private final Gson gson = new Gson();

    private static final String LOGGING_TAGS = SystemEnv.LOGGING_TAGS.getValue();

    @Autowired
    private ChangesService changesService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private HealthService healthService;

    @Autowired
    public CallbackConsumer(HealthStatusRepository healthStatusRepository, TargetRepository targetRepository) {
        this.healthStatusRepository = healthStatusRepository;
        this.targetRepository = targetRepository;
    }

    @JmsListener(destination = QUEUE_HEALTH_CALLBACK)
    public void callback(HealthStatus healthStatus) {
        try {
            if (healthStatus != null) {
                HealthStatus tempHealthStatus = healthStatusRepository.findBySourceAndTargetId(healthStatus.getSource(), healthStatus.getTarget().getId());
                if (tempHealthStatus == null) {
                    tempHealthStatus = healthStatus;
                    Target target = targetRepository.findOne(healthStatus.getTarget().getId());
                    tempHealthStatus.setTarget(target);
                }

                tempHealthStatus.setStatus(healthStatus.getStatus());
                tempHealthStatus.setStatusDetailed(healthStatus.getStatusDetailed());
                healthStatusRepository.save(tempHealthStatus);

                Target target = tempHealthStatus.getTarget();
                tempHealthStatus.getTarget().getAllEnvironments().forEach(e ->
                        changesService.register(e, target, String.valueOf(versionService.incrementVersion(String.valueOf(e.getId())))));

                Map<String, String> mapLog = new HashMap<>();
                mapLog.put("class", CallbackConsumer.class.getSimpleName());
                mapLog.put("queue", QUEUE_HEALTH_CALLBACK);
                mapLog.put("healthStatus_source", tempHealthStatus.getSource());
                mapLog.put("healthStatus_statusDetailed", tempHealthStatus.getStatusDetailed());
                mapLog.put("healthStatus_status", tempHealthStatus.getStatus().name());
                mapLog.put("healthStatus_target", tempHealthStatus.getTarget().getName());
                mapLog.put("tags", LOGGING_TAGS);

                LOGGER.info(gson.toJson(mapLog));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @JmsListener(destination = QUEUE_HEALTH_REGISTER)
    public void registerListener(String message) {
        healthService.put(message);
    }
}
