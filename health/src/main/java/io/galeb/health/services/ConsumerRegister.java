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

package io.galeb.health.services;

import io.galeb.core.entity.Target;
import io.galeb.core.enums.SystemEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageListener;

@Component
public class ConsumerRegister {

    private static final String QUEUE_NAME =
            SystemEnv.QUEUE_NAME.getValue() + "_" +
            SystemEnv.ENVIRONMENT_NAME.getValue().replaceAll("[ ]+", "_").toLowerCase();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConnectionFactory connectionFactory;
    private final HealthCheckerService healthCheckerService;

    @Autowired
    public ConsumerRegister(ConnectionFactory connectionFactory, HealthCheckerService healthCheckerService) {
        this.connectionFactory = connectionFactory;
        this.healthCheckerService = healthCheckerService;
    }

    public void startMessageListener() {
        DefaultMessageListenerContainer messageListener = new DefaultMessageListenerContainer();

        messageListener.setConcurrency("5-5");
        messageListener.setDestinationName(QUEUE_NAME);
        messageListener.setConnectionFactory(connectionFactory);
        messageListener.setMessageListener((MessageListener) message -> {
            try {
                healthCheckerService.check(message.getBody(Target.class));
            } catch (JMSException e) {
                logger.error(e.getMessage(), e);
            }
        });
        messageListener.setPubSubDomain(false);
        messageListener.initialize();
        messageListener.start();
    }
}
