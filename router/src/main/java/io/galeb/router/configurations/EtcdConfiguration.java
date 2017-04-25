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

package io.galeb.router.configurations;

import io.galeb.core.configuration.SystemEnvs;
import io.galeb.router.kv.EtcdClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.zalando.boot.etcd.EtcdException;

import java.util.concurrent.ExecutionException;

@Configuration
@Profile({ "production" })
public class EtcdConfiguration {

    private final Log logger = LogFactory.getLog(this.getClass());

    private static final String ETCD_SERVER = SystemEnvs.ETCD_SERVER.getValue();

    @Bean
    public EtcdClient etcdClient() throws EtcdException, ExecutionException, InterruptedException {
        final EtcdClient etcdClient = new EtcdClient(ETCD_SERVER);
        logger.info("Using " + etcdClient);
        return etcdClient;
    }

}
