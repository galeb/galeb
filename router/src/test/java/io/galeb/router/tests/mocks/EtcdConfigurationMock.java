package io.galeb.router.tests.mocks;

import static org.mockito.Mockito.*;

import io.galeb.router.services.ExternalData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.zalando.boot.etcd.EtcdClient;
import org.zalando.boot.etcd.EtcdException;
import org.zalando.boot.etcd.EtcdNode;
import org.zalando.boot.etcd.EtcdResponse;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

@Configuration
@Profile({ "test" })
public class EtcdConfigurationMock {

    private static final Log LOGGER = LogFactory.getLog(EtcdConfigurationMock.class);

    private EtcdClient etcdClientMocked = mock(EtcdClient.class);

    @Bean("etcdClient")
    public EtcdClient etcdClient() throws EtcdException {
        LOGGER.info("Using " + this.getClass().getSimpleName());

        String theVirtualhostKey = ExternalData.VIRTUALHOSTS_KEY + "/test.com";
        String allowKey = theVirtualhostKey + "/allow";
        String rulesKey = theVirtualhostKey + "/rules";
        String theRuleKey = rulesKey + "/" + Base64.getEncoder().encodeToString("/".getBytes(Charset.defaultCharset()));
        String theOrderKey = theRuleKey + "/order";
        String theRuleTargetKey = theRuleKey + "/target";
        String theRuleTypeKey = theRuleKey + "/type";
        String thePoolKey = ExternalData.POOLS_KEY + "/0";
        String poolTargetsKey = thePoolKey + "/targets";
        String thePoolTargetKey = poolTargetsKey + "/0";
        String loadBalancePolicyKey = thePoolKey + "/loadbalance";

        EtcdResponse etcdResponse = mock(EtcdResponse.class);
        when(etcdClientMocked.get(anyString())).thenReturn(etcdResponse);
        when(etcdClientMocked.get(anyString(), anyBoolean())).thenReturn(etcdResponse);

        EtcdNode orderNode = new EtcdNode();
        orderNode.setKey(theOrderKey);
        orderNode.setValue("0");
        when(etcdClientMocked.get(theOrderKey).getNode()).thenReturn(orderNode);
        when(etcdClientMocked.get(eq(theOrderKey), anyBoolean()).getNode()).thenReturn(orderNode);

        EtcdNode targetNode = new EtcdNode();
        targetNode.setKey(theRuleTargetKey);
        targetNode.setValue("0");
        when(etcdClientMocked.get(theRuleTargetKey).getNode()).thenReturn(targetNode);
        when(etcdClientMocked.get(eq(theRuleTargetKey), anyBoolean()).getNode()).thenReturn(targetNode);

        EtcdNode typeNode = new EtcdNode();
        typeNode.setKey(theRuleTypeKey);
        typeNode.setValue("PATH");
        when(etcdClientMocked.get(theRuleTypeKey).getNode()).thenReturn(typeNode);
        when(etcdClientMocked.get(eq(theRuleTypeKey), anyBoolean()).getNode()).thenReturn(typeNode);

        EtcdNode ruleNode = new EtcdNode();
        ruleNode.setKey(theRuleKey);
        ruleNode.setDir(true);
        ruleNode.setNodes(Arrays.asList(orderNode, targetNode, typeNode));
        when(etcdClientMocked.get(theRuleKey).getNode()).thenReturn(ruleNode);
        when(etcdClientMocked.get(eq(theRuleKey), anyBoolean()).getNode()).thenReturn(ruleNode);

        EtcdNode allowNode = new EtcdNode();
        allowNode.setKey(allowKey);
        allowNode.setValue("127.0.0.0/8,172.16.0.1");
        when(etcdClientMocked.get(allowKey).getNode()).thenReturn(allowNode);
        when(etcdClientMocked.get(eq(allowKey), anyBoolean()).getNode()).thenReturn(allowNode);

        EtcdNode rulesNode = new EtcdNode();
        rulesNode.setKey(rulesKey);
        rulesNode.setDir(true);
        rulesNode.setNodes(Arrays.asList(ruleNode, allowNode));
        when(etcdClientMocked.get(rulesKey).getNode()).thenReturn(rulesNode);
        when(etcdClientMocked.get(eq(rulesKey), anyBoolean()).getNode()).thenReturn(rulesNode);

        EtcdNode virtualhostNode = new EtcdNode();
        virtualhostNode.setKey(theVirtualhostKey);
        virtualhostNode.setDir(true);
        virtualhostNode.setNodes(new ArrayList<>());
        when(etcdClientMocked.get(theVirtualhostKey).getNode()).thenReturn(virtualhostNode);
        when(etcdClientMocked.get(eq(theVirtualhostKey), anyBoolean()).getNode()).thenReturn(virtualhostNode);

        EtcdNode virtualhostsNode = new EtcdNode();
        virtualhostsNode.setKey(ExternalData.VIRTUALHOSTS_KEY);
        virtualhostsNode.setDir(true);
        virtualhostsNode.setNodes(Collections.singletonList(virtualhostNode));
        when(etcdClientMocked.get(ExternalData.VIRTUALHOSTS_KEY).getNode()).thenReturn(virtualhostsNode);
        when(etcdClientMocked.get(eq(ExternalData.VIRTUALHOSTS_KEY), anyBoolean()).getNode()).thenReturn(virtualhostsNode);

        EtcdNode poolNode = new EtcdNode();
        poolNode.setKey(thePoolKey);
        poolNode.setDir(true);
        poolNode.setNodes(new ArrayList<>());
        when(etcdClientMocked.get(thePoolKey).getNode()).thenReturn(poolNode);
        when(etcdClientMocked.get(eq(thePoolKey), anyBoolean()).getNode()).thenReturn(poolNode);

        EtcdNode poolTargetNode = new EtcdNode();
        poolTargetNode.setKey(thePoolTargetKey);
        poolTargetNode.setValue("http://127.0.0.1:8080");
        when(etcdClientMocked.get(thePoolTargetKey).getNode()).thenReturn(poolTargetNode);
        when(etcdClientMocked.get(eq(thePoolTargetKey), anyBoolean()).getNode()).thenReturn(poolTargetNode);

        EtcdNode loadbalancepolicyNode = new EtcdNode();
        loadbalancepolicyNode.setKey(loadBalancePolicyKey);
        loadbalancepolicyNode.setValue("ROUNDROBIN");
        when(etcdClientMocked.get(loadBalancePolicyKey).getNode()).thenReturn(loadbalancepolicyNode);
        when(etcdClientMocked.get(eq(loadBalancePolicyKey), anyBoolean()).getNode()).thenReturn(loadbalancepolicyNode);

        EtcdNode poolTargetsNode = new EtcdNode();
        poolTargetsNode.setKey(poolTargetsKey);
        poolTargetsNode.setDir(true);
        poolTargetsNode.setNodes(Arrays.asList(poolTargetNode, loadbalancepolicyNode));
        when(etcdClientMocked.get(poolTargetsKey).getNode()).thenReturn(poolTargetsNode);
        when(etcdClientMocked.get(eq(poolTargetsKey), anyBoolean()).getNode()).thenReturn(poolTargetsNode);

        EtcdNode poolsNode = new EtcdNode();
        poolsNode.setKey(ExternalData.POOLS_KEY);
        poolsNode.setDir(true);
        poolsNode.setNodes(Collections.singletonList(poolNode));
        when(etcdClientMocked.get(ExternalData.POOLS_KEY).getNode()).thenReturn(poolsNode);
        when(etcdClientMocked.get(eq(ExternalData.POOLS_KEY), anyBoolean()).getNode()).thenReturn(poolsNode);

        EtcdNode rootNode = new EtcdNode();
        rootNode.setKey(ExternalData.PREFIX_KEY);
        rootNode.setDir(true);
        rootNode.setNodes(Arrays.asList(virtualhostsNode, poolsNode));
        when(etcdClientMocked.get(ExternalData.PREFIX_KEY).getNode()).thenReturn(rootNode);
        when(etcdClientMocked.get(eq(ExternalData.PREFIX_KEY), anyBoolean()).getNode()).thenReturn(rootNode);

        return etcdClientMocked;
    }
}
