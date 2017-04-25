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

package io.galeb.health.configurations;

import io.galeb.core.configuration.SystemEnvs;
import org.apache.activemq.artemis.api.core.BroadcastGroupConfiguration;
import org.apache.activemq.artemis.api.core.DiscoveryGroupConfiguration;
import org.apache.activemq.artemis.api.core.JGroupsFileBroadcastEndpointFactory;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy;
import org.apache.activemq.artemis.core.config.ClusterConnectionConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.server.cluster.impl.MessageLoadBalancingType;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants.SERVER_ID_PROP_NAME;

@Configuration
@EnableJms
public class JMSConfiguration {

    private static final String JMS_BROKER_URL = "vm://0?useShutdownHook=false";

    private boolean enableHa = Boolean.parseBoolean(SystemEnvs.ENABLE_HA.getValue());

    @Bean
    public DefaultJmsListenerContainerFactory containerFactory(DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(JMS_BROKER_URL);
        if (enableHa) {
            connectionFactory.setConnectionLoadBalancingPolicyClassName(RoundRobinConnectionLoadBalancingPolicy.class.getName());
        }
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        cachingConnectionFactory.setSessionCacheSize(100);
        cachingConnectionFactory.setCacheConsumers(true);
        configurer.configure(factory, cachingConnectionFactory);
        return factory;
    }

    @Bean
    public ArtemisConfigurationCustomizer artemisConfigurationCustomizer() {
        return configuration -> {
            if (enableHa) {
                ClusterConnectionConfiguration clusterConnectionConfiguration = new ClusterConnectionConfiguration()
                        .setName("mycluster")
                        .setDiscoveryGroupName("mydistgroup")
                        .setAddress("jms")
                        .setMessageLoadBalancingType(MessageLoadBalancingType.STRICT)
                        .setMaxHops(1)
                        .setRetryInterval(500)
                        .setDuplicateDetection(true);

                JGroupsFileBroadcastEndpointFactory endpointFactory = new JGroupsFileBroadcastEndpointFactory()
                        .setChannelName(SystemEnvs.CLUSTER_ID.getValue())
                        .setFile("jgroups.xml");

                BroadcastGroupConfiguration broadcastGroupConfiguration = new BroadcastGroupConfiguration()
                        .setName("my-broadcast-group")
                        .setEndpointFactory(endpointFactory)
                        .setBroadcastPeriod(5000)
                        .setConnectorInfos(Collections.singletonList("netty-connector"));

                DiscoveryGroupConfiguration discoveryGroupConfiguration = new DiscoveryGroupConfiguration()
                        .setName("mydistgroup")
                        .setRefreshTimeout(10000)
                        .setBroadcastEndpointFactory(endpointFactory);

                configuration.addClusterConfiguration(clusterConnectionConfiguration)
                        .addDiscoveryGroupConfiguration("mydistgroup", discoveryGroupConfiguration)
                        .addBroadcastGroupConfiguration(broadcastGroupConfiguration)
                        .addConnectorConfiguration("netty-connector",
                                new TransportConfiguration(NettyConnectorFactory.class.getName(), new HashMap<>()))
                        .addAcceptorConfiguration(new TransportConfiguration(NettyAcceptorFactory.class.getName()));

            }
            configuration.setPersistenceEnabled(false)
                    .setSecurityEnabled(false)
                    .addConnectorConfiguration("invm-connector",
                        new TransportConfiguration(InVMConnectorFactory.class.getName(), generateInVMParams()));
        };
    }

    private Map<String, Object> generateInVMParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(SERVER_ID_PROP_NAME, 0);
        return params;
    }

}
