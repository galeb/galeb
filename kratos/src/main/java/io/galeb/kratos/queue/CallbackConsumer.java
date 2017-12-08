package io.galeb.kratos.queue;

import io.galeb.core.entity.Target;
import io.galeb.kratos.repository.HealthStatusRepository;
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

    @Autowired
    public CallbackConsumer(HealthStatusRepository healthStatusRepository) {
        this.healthStatusRepository = healthStatusRepository;
    }

    @JmsListener(destination = QUEUE_HEALTH_CALLBACK)
    public void callback(Target target) {
        try {
            if (target != null) {
                healthStatusRepository.save(target.getHealthStatus());
                LOGGER.warn("HealthStatus (from target " + target.getName() + ") updated.");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
