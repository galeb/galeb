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

package io.galeb.router.tests.mocks;

import io.galeb.router.discovery.etcd.EtcdClient;
import io.galeb.router.discovery.etcd.EtcdNode;
import io.galeb.router.discovery.etcd.EtcdResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Profile({ "test" })
public class EtcdConfigurationMock {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final EtcdClient etcdClientMocked = mock(EtcdClient.class);

    @Bean("etcdClient")
    public EtcdClient etcdClient() throws ExecutionException, InterruptedException {
        logger.warn("Using " + this.getClass().getSimpleName());

        EtcdResponse anyEtcdResponse = new EtcdResponse();
        anyEtcdResponse.setNode(new EtcdNode());
        when(etcdClientMocked.put(any(EtcdNode.class))).thenReturn(anyEtcdResponse);
        when(etcdClientMocked.get(anyString(), anyBoolean())).thenReturn(anyEtcdResponse);
        when(etcdClientMocked.get(anyString())).thenReturn(anyEtcdResponse);

        return etcdClientMocked;
    }
}
