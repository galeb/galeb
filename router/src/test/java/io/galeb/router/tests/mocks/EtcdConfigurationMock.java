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

import static org.mockito.Mockito.*;

import io.galeb.core.enums.EnumRuleType;
import io.galeb.router.discovery.etcd.EtcdClient;
import io.galeb.router.discovery.etcd.EtcdExternalDataService;
import io.galeb.router.discovery.etcd.EtcdNode;
import io.galeb.router.discovery.etcd.EtcdResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Configuration
@Profile({ "test" })
public class EtcdConfigurationMock {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final EtcdClient etcdClientMocked = mock(EtcdClient.class);
    private final Map<String, EtcdNode> nodes = new HashMap<>();

    @Bean("etcdClient")
    public EtcdClient etcdClient() throws ExecutionException, InterruptedException {
        logger.warn("Using " + this.getClass().getSimpleName());

        String theVirtualhostKey = EtcdExternalDataService.VIRTUALHOSTS_KEY + "/test.com";
        String allowKey = theVirtualhostKey + "/allow";
        String rulesKey = theVirtualhostKey + "/rules";
        String theRuleKey = rulesKey + "/" + Base64.getEncoder().encodeToString("/".getBytes(Charset.defaultCharset()));
        String theOrderKey = theRuleKey + "/order";
        String theRuleTargetKey = theRuleKey + "/target";
        String theRuleTypeKey = theRuleKey + "/type";
        String thePoolKey = EtcdExternalDataService.POOLS_KEY + "/0";
        String poolTargetsKey = thePoolKey + "/targets";
        String thePoolTargetKey = poolTargetsKey + "/0";
        String loadBalancePolicyKey = thePoolKey + "/loadbalance";

        EtcdNode orderNode = new EtcdNode();
        orderNode.setKey(theOrderKey);
        orderNode.setValue("0");
        nodes.put(theOrderKey, orderNode);

        EtcdNode ruleTargetNode = new EtcdNode();
        ruleTargetNode.setKey(theRuleTargetKey);
        ruleTargetNode.setValue("0");
        nodes.put(theRuleTargetKey, ruleTargetNode);

        EtcdNode typeNode = new EtcdNode();
        typeNode.setKey(theRuleTypeKey);
        typeNode.setValue(EnumRuleType.PATH.toString());
        nodes.put(theRuleTypeKey, typeNode);

        EtcdNode ruleNode = new EtcdNode();
        ruleNode.setKey(theRuleKey);
        ruleNode.setDir(true);
        ruleNode.setNodes(Arrays.asList(orderNode, ruleTargetNode, typeNode));
        nodes.put(theRuleKey, ruleNode);

        EtcdNode allowNode = new EtcdNode();
        allowNode.setKey(allowKey);
        allowNode.setValue("127.0.0.0/8,0:0:0:0:0:0:0:1/128,10.*.*.*");
        nodes.put(allowKey, allowNode);

        EtcdNode rulesNode = new EtcdNode();
        rulesNode.setKey(rulesKey);
        rulesNode.setDir(true);
        rulesNode.setNodes(Collections.singletonList(ruleNode));
        nodes.put(rulesKey, rulesNode);

        EtcdNode virtualhostNode = new EtcdNode();
        virtualhostNode.setKey(theVirtualhostKey);
        virtualhostNode.setDir(true);
        virtualhostNode.setNodes(Arrays.asList(rulesNode, allowNode));
        nodes.put(theVirtualhostKey, virtualhostNode);

        EtcdNode virtualhostsNode = new EtcdNode();
        virtualhostsNode.setKey(EtcdExternalDataService.VIRTUALHOSTS_KEY);
        virtualhostsNode.setDir(true);
        virtualhostsNode.setNodes(Collections.singletonList(virtualhostNode));
        nodes.put(EtcdExternalDataService.VIRTUALHOSTS_KEY, virtualhostsNode);

        EtcdNode poolTargetNode = new EtcdNode();
        poolTargetNode.setKey(thePoolTargetKey);
        poolTargetNode.setValue("http://127.0.0.1:8080");
        nodes.put(thePoolTargetKey, poolTargetNode);

        EtcdNode loadbalancepolicyNode = new EtcdNode();
        loadbalancepolicyNode.setKey(loadBalancePolicyKey);
        loadbalancepolicyNode.setValue("ROUNDROBIN");
        nodes.put(loadBalancePolicyKey, loadbalancepolicyNode);

        EtcdNode poolTargetsNode = new EtcdNode();
        poolTargetsNode.setKey(poolTargetsKey);
        poolTargetsNode.setDir(true);
        poolTargetsNode.setNodes(Collections.singletonList(poolTargetNode));
        nodes.put(poolTargetsKey, poolTargetsNode);

        EtcdNode poolNode = new EtcdNode();
        poolNode.setKey(thePoolKey);
        poolNode.setDir(true);
        poolNode.setNodes(Arrays.asList(poolTargetsNode, loadbalancepolicyNode));
        nodes.put(thePoolKey, poolNode);

        EtcdNode poolsNode = new EtcdNode();
        poolsNode.setKey(EtcdExternalDataService.POOLS_KEY);
        poolsNode.setDir(true);
        poolsNode.setNodes(Collections.singletonList(poolNode));
        nodes.put(EtcdExternalDataService.POOLS_KEY, poolsNode);

        EtcdNode rootNode = new EtcdNode();
        rootNode.setKey(EtcdExternalDataService.PREFIX_KEY);
        rootNode.setDir(true);
        rootNode.setNodes(Arrays.asList(virtualhostsNode, poolsNode));
        nodes.put(EtcdExternalDataService.PREFIX_KEY, rootNode);

        when(etcdClientMocked.get(anyString(), anyBoolean())).thenAnswer(invoc -> {
            String key = invoc.getArgumentAt(0, String.class);
            final EtcdResponse etcResponse = new EtcdResponse();
            etcResponse.setNode(nodes.get(key));
            return etcResponse;
        });

        EtcdResponse anyEtcdResponse = new EtcdResponse();
        anyEtcdResponse.setNode(new EtcdNode());
        when(etcdClientMocked.put(any(EtcdNode.class))).thenReturn(anyEtcdResponse);
        when(etcdClientMocked.get(anyString())).thenReturn(anyEtcdResponse);

        return etcdClientMocked;
    }
}
