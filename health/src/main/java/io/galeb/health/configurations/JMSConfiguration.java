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

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

import javax.jms.ConnectionFactory;

@Configuration
@EnableJms
public class JMSConfiguration {

    @Bean
    public DefaultJmsListenerContainerFactory containerFactory(DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory targetConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(targetConnectionFactory);
        cachingConnectionFactory.setSessionCacheSize(100);
        cachingConnectionFactory.setCacheConsumers(true);
        configurer.configure(factory, cachingConnectionFactory);
        return factory;
    }

}
