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

package io.galeb.kratos.configurations;

import io.galeb.core.enums.SystemEnv;
import org.apache.activemq.artemis.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import static org.springframework.jms.support.destination.JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT;

@SuppressWarnings("Duplicates")
@Configuration
@EnableJms
public class JMSConfiguration {

    private static final long   JMS_TIMEOUT = Long.parseLong(SystemEnv.JMS_TIMEOUT.getValue());
    private static final String BROKER_CONN = SystemEnv.BROKER_CONN.getValue();
    private static final String BROKER_USER = SystemEnv.BROKER_USER.getValue();
    private static final String BROKER_PASS = SystemEnv.BROKER_PASS.getValue();
    private static final boolean BROKER_HA  = Boolean.parseBoolean(SystemEnv.BROKER_HA.getValue());

    // Additional Broker Configuration
    private static final boolean BROKER_BLOCKDURABLESEND   = Boolean.parseBoolean(SystemEnv.BROKER_BLOCKDURABLESEND.getValue());
    private static final int     BROKER_CONSUMERWINDOWSIZE = Integer.parseInt(SystemEnv.BROKER_CONSUMERWINDOWSIZE.getValue());

    @Bean(name="connectionFactory")
    public CachingConnectionFactory cachingConnectionFactory() throws JMSException {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_CONN);
        connectionFactory.setUser(BROKER_USER);
        connectionFactory.setPassword(BROKER_PASS);
        connectionFactory.setBlockOnDurableSend(BROKER_BLOCKDURABLESEND);
        connectionFactory.setConsumerWindowSize(BROKER_CONSUMERWINDOWSIZE);
        if (BROKER_HA) {
            connectionFactory.setConnectionLoadBalancingPolicyClassName(RoundRobinConnectionLoadBalancingPolicy.class.getName());
        }
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(100);
        cachingConnectionFactory.setCacheConsumers(true);
        return cachingConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setDeliveryPersistent(false);
        jmsTemplate.setReceiveTimeout(RECEIVE_TIMEOUT_NO_WAIT);
        jmsTemplate.setTimeToLive(JMS_TIMEOUT);
        return jmsTemplate;
    }

    @Qualifier("templateTopic")
    @Bean
    public JmsTemplate jmsTemplateTopic(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplateTopic = new JmsTemplate(connectionFactory);
        jmsTemplateTopic.setPubSubDomain(true);
        return jmsTemplateTopic;
    }

}
