package io.galeb.health.broker;

import io.galeb.health.SystemEnvs;
import io.galeb.manager.entity.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import java.util.Arrays;
import java.util.Base64;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

@Component
public class Producer {

    @SuppressWarnings("FieldCanBeLocal")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JmsTemplate jmsTemplate;

    @Autowired
    public Producer(final JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        logger.info(this.getClass().getSimpleName() + " started");
    }

    public void send(Target target) {
        jmsTemplate.send(SystemEnvs.QUEUE_NAME.getValue(), session -> {
            Message message = session.createObjectMessage(target);
            String id = "ID:" + Arrays.toString(Base64.getEncoder().encode(target.getName().getBytes())).replaceAll("=", "");
            id = id + Math.ceil(System.currentTimeMillis() / 10000L); // uniq per 10s
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), id);
            message.setJMSMessageID(id);
            return message;
        });
    }
}
