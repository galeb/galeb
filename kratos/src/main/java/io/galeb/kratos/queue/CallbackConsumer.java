package io.galeb.kratos.queue;

import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.dto.TargetDTO;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.core.services.ChangesService;
import io.galeb.core.services.VersionService;
import io.galeb.kratos.repository.HealthStatusRepository;
import io.galeb.kratos.repository.TargetRepository;
import io.galeb.kratos.services.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class CallbackConsumer {

    private static final String QUEUE_HEALTH_CALLBACK = "health-callback";
    private static final String QUEUE_HEALTH_REGISTER = "health-register";

    private final HealthStatusRepository healthStatusRepository;
    private final TargetRepository targetRepository;

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
    public void callback(TargetDTO targetDTO) {
        try {
            if (targetDTO != null) {
                final String correlation = targetDTO.getCorrelation();
                final HealthStatus healthStatus = targetDTO.getTarget().getHealthStatus()
                    .stream().findAny().orElse(new HealthStatus());
                final String source = healthStatus.getSource();
                final Long targetId = targetDTO.getTarget().getId();

                HealthStatus tempHealthStatus = healthStatusRepository.findBySourceAndTargetId(source, targetId);
                if (tempHealthStatus == null) {
                    tempHealthStatus = healthStatus;
                    Target target = targetRepository.findOne(targetId);
                    tempHealthStatus.setTarget(target);
                }

                tempHealthStatus.setStatus(healthStatus.getStatus());
                tempHealthStatus.setStatusDetailed(healthStatus.getStatusDetailed());
                healthStatusRepository.save(tempHealthStatus);

                Target target = tempHealthStatus.getTarget();
                tempHealthStatus.getTarget().getAllEnvironments().forEach(e ->
                        changesService.register(e, target, String.valueOf(versionService.incrementVersion(String.valueOf(e.getId())))));

                JsonEventToLogger jsonLogger =  new JsonEventToLogger(this.getClass());
                jsonLogger.put("queue", QUEUE_HEALTH_CALLBACK);
                jsonLogger.put("correlation", correlation);
                jsonLogger.put("short_message", "Processing healthStatus from callback queue");
                jsonLogger.put("healthStatus_source", tempHealthStatus.getSource());
                jsonLogger.put("healthStatus_statusDetailed", tempHealthStatus.getStatusDetailed());
                jsonLogger.put("healthStatus_status", tempHealthStatus.getStatus().name());
                jsonLogger.put("healthStatus_target", tempHealthStatus.getTarget().getName());
                jsonLogger.sendInfo();
            }
        } catch (Exception e) {
            JsonEventToLogger jsonLogger = new JsonEventToLogger(this.getClass());
            jsonLogger.put("queue", QUEUE_HEALTH_CALLBACK);
            jsonLogger.put("short_message", "Error during process healthStatus from callback queue - target: " + targetDTO.getTarget().getId());
            jsonLogger.sendError(e);
        }
    }

    @JmsListener(destination = QUEUE_HEALTH_REGISTER)
    public void registerListener(String message) {
        healthService.put(message);
    }
}
