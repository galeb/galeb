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

package io.galeb.health.broker;

import io.galeb.core.configuration.SystemEnvs;
import io.galeb.core.entity.Target;
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
