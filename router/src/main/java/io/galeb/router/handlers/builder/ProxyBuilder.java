package io.galeb.router.handlers.builder;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;

import io.galeb.core.entity.BalancePolicy;
import io.galeb.core.entity.Pool;
import io.galeb.core.enums.SystemEnv;
import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.HostSelectorLookup;
import io.galeb.router.client.hostselectors.RoundRobinHostSelector;
import io.undertow.client.UndertowClient;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.proxy.ExclusivityChecker;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.Headers;

public class ProxyBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ProxyBuilder.class);

    public static final String PROP_CONN_PER_THREAD         = "connPerThread";
    public static final String PROP_DISCOVERED_MEMBERS_SIZE = "discoveredMembersSize";

    private static final int maxRequestTime = Integer.parseInt(SystemEnv.POOL_MAX_REQUEST_TIME.getValue());
    private static final boolean reuseXForwarded = Boolean.parseBoolean(SystemEnv.REUSE_XFORWARDED.getValue());
    private static final boolean rewriteHostHeader = Boolean.parseBoolean(SystemEnv.REWRITE_HOST_HEADER.getValue());

    //context.getBean("undertowOptionMap", OptionMap.class);
    public static ProxyHandler buildProxy(Pool pool, OptionMap options) {
        logger.info("creating pool " + pool.getName());
        ExtendedLoadBalancingProxyClient proxyClient = getProxyClient(pool);

        pool.getTargets().forEach(target -> {
            String value = target.getName();
            URI uri = URI.create(target.getName());
            proxyClient.addHost(uri, options);
            logger.info("[pool:" + pool.getName() + "] added Target " + value);
        });

        ProxyHandler proxyHandler = ProxyHandler.builder()
            .setProxyClient(proxyClient)
            .setMaxRequestTime(maxRequestTime)
            .setNext(badGatewayHandler())
            .setRewriteHostHeader(rewriteHostHeader)
            .setReuseXForwarded(reuseXForwarded)
            .build();

        return proxyHandler;
    }

    private static ExtendedLoadBalancingProxyClient getProxyClient(Pool pool) {
        final HostSelector hostSelector = defineHostSelector(pool);
        logger.info("[Pool " + pool.getName() + "] HostSelector: " + hostSelector.getClass().getSimpleName());

        final ExclusivityChecker exclusivityChecker = exclusivityCheckerExchange -> exclusivityCheckerExchange.getRequestHeaders().contains(Headers.UPGRADE);
        return new ExtendedLoadBalancingProxyClient(UndertowClient.getInstance(), exclusivityChecker, hostSelector)
                        .setTtl(Integer.parseInt(SystemEnv.POOL_CONN_TTL.getValue()))
                        .setConnectionsPerThread(getConnPerThread(pool))
                        .setSoftMaxConnectionsPerThread(Integer.parseInt(SystemEnv.POOL_SOFTMAXCONN.getValue()));
    }

    private static int getConnPerThread(Pool pool) {
        int poolMaxConn = Integer.parseInt(SystemEnv.POOL_MAXCONN.getValue());
        int connPerThread = poolMaxConn / Integer.parseInt(SystemEnv.IO_THREADS.getValue());
        String propConnPerThread = pool.getProperties().get(PROP_CONN_PER_THREAD);
        if (propConnPerThread != null) {
            try {
                connPerThread = Integer.parseInt(propConnPerThread);
            } catch (NumberFormatException ignore) {}
        }
        String discoveredMembersStr = pool.getProperties().get(PROP_DISCOVERED_MEMBERS_SIZE);
        float discoveredMembers = 1.0f;
        if (discoveredMembersStr != null && !"".equals(discoveredMembersStr)) {
            discoveredMembers = Float.parseFloat(discoveredMembersStr);
        }
        float discoveryMembersSize = Math.max(discoveredMembers, 1.0f);
        connPerThread = Math.round((float) connPerThread / discoveryMembersSize);
        return connPerThread;
    }

    private static HostSelector defineHostSelector(Pool pool) {
        BalancePolicy hostSelectorName = pool.getBalancePolicy();
        if (hostSelectorName != null) {
            return HostSelectorLookup.getHostSelector(hostSelectorName.getName());
        }
        return new RoundRobinHostSelector();
    }

    private static HttpHandler badGatewayHandler() {
        return exchange -> exchange.setStatusCode(502);
    }

}
