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
import io.galeb.core.entity.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Message;

import static org.apache.activemq.artemis.api.core.Message.HDR_DUPLICATE_DETECTION_ID;

@Component
public class TargetStamper {

    public enum HcState {
        FAIL,
        UNKNOWN,
        OK
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PROP_HEALTHCHECK_RETURN = "hcBody";
    public static final String PROP_HEALTHCHECK_PATH   = "hcPath";
    public static final String PROP_HEALTHCHECK_HOST   = "hcHost";
    public static final String PROP_HEALTHCHECK_CODE   = "hcStatusCode";
    public static final String PROP_HEALTHY            = "healthy";
    public static final String PROP_STATUS_DETAILED    = "status_detailed";

    private final JmsTemplate jmsTemplate;

    @Autowired
    public TargetStamper(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void stamp(Target target) {
        String targetStr = new Gson().toJson(target, Target.class);
        jmsTemplate.send("health-callback", session -> {
            Message message = session.createObjectMessage(targetStr);
            String uniqueId = "ID:" + target.getName() + "-" + System.currentTimeMillis();
            message.setStringProperty("_HQ_DUPL_ID", uniqueId);
            message.setJMSMessageID(uniqueId);
            message.setStringProperty(HDR_DUPLICATE_DETECTION_ID.toString(), uniqueId);

            logger.info("JMSMessageID: " + uniqueId + " - Target " + target.getName());
            return message;
        });
    }

}
