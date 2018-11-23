package io.galeb.health.scheduler;

import io.galeb.core.enums.SystemEnv;
import io.galeb.health.util.CallBackQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class ScheduledRegister {

    private final CallBackQueue callBackQueue;

    private static final String ZONE_ID = SystemEnv.ZONE_ID.getValue();

    @Autowired
    public ScheduledRegister(final CallBackQueue callBackQueue) {
        this.callBackQueue = callBackQueue;
    }

    @Scheduled(fixedDelay = 10000L)
    public void registerQueue() {
        callBackQueue.register(ZONE_ID);
    }
}
