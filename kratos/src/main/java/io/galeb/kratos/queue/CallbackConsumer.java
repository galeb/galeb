package io.galeb.kratos.queue;

import io.galeb.core.entity.HealthStatus;
import io.galeb.core.entity.Target;
import io.galeb.kratos.repository.HealthStatusRepository;
import io.galeb.kratos.repository.TargetRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class CallbackConsumer {

    private static final Log LOGGER = LogFactory.getLog(CallbackConsumer.class);

    private static final String QUEUE_HEALTH_CALLBACK = "health-callback";

    private final HealthStatusRepository healthStatusRepository;
    private final TargetRepository targetRepository;


    @Autowired
    public CallbackConsumer(HealthStatusRepository healthStatusRepository, TargetRepository targetRepository) {
        this.healthStatusRepository = healthStatusRepository;
        this.targetRepository = targetRepository;
    }

    @JmsListener(destination = QUEUE_HEALTH_CALLBACK)
    public void callback(HealthStatus healthStatus) {
        try {
            if (healthStatus != null) {
                Target target = targetRepository.findOne(healthStatus.getTarget().getId());
                healthStatus.setTarget(target);
                healthStatusRepository.save(healthStatus);
                LOGGER.warn("HealthStatus [source: " + healthStatus.getSource() +  "] (from target " + healthStatus.getTarget().getName() + ") updated.");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
