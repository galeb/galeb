/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.health.util;

import com.google.gson.Gson;
import io.galeb.core.entity.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import java.util.HashMap;
import java.util.Map;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

@Component
public class CallBackQueue {

    private static final String HEALTH_CALLBACK_QUEUE = "health-callback";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JmsTemplate jmsTemplate;

    private final Gson gson = new Gson();

    @Autowired
    public CallBackQueue(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void update(HealthStatus healthStatus) {
        try {
            jmsTemplate.send(HEALTH_CALLBACK_QUEUE, session -> {
                Message message = session.createObjectMessage(healthStatus);
                String uniqueId = "ID:" + healthStatus.getTarget().getName() + "-" + healthStatus.getSource() + "_" + System.currentTimeMillis();
                message.setStringProperty("_HQ_DUPL_ID", uniqueId);
                message.setJMSMessageID(uniqueId);
                message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

                Map<String, String> mapLog = new HashMap<>();
                mapLog.put("class", CallBackQueue.class.getSimpleName().toString());
                mapLog.put("queue", HEALTH_CALLBACK_QUEUE);
                mapLog.put("jmsMessageId", uniqueId);
                mapLog.put("healthStatus_source", healthStatus.getSource());
                mapLog.put("healthStatus_statusDetailed", healthStatus.getStatusDetailed());
                mapLog.put("healthStatus_status", healthStatus.getStatus().name());
                mapLog.put("healthStatus_target", healthStatus.getTarget().getName());

                logger.info(gson.toJson(mapLog));
                return message;
            });
        } catch (JmsException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
