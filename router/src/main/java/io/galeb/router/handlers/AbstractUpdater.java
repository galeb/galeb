package io.galeb.router.handlers;

import io.galeb.router.client.ExtendedProxyClient;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public abstract class AbstractUpdater {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final NameVirtualHostHandler nameVirtualHostHandler;

    public AbstractUpdater(final NameVirtualHostHandler nameVirtualHostHandler) {
        this.nameVirtualHostHandler = nameVirtualHostHandler;
    }

    private void cleanUpNameVirtualHostHandler(String virtualhost) {
        final HttpHandler handler = nameVirtualHostHandler.getHosts().get(virtualhost);
        if (handler instanceof RuleTargetHandler) {
            HttpHandler ruleTargetNextHandler = ((RuleTargetHandler) handler).getNext();
            if (ruleTargetNextHandler instanceof IPAddressAccessControlHandler) {
                ruleTargetNextHandler = ((IPAddressAccessControlHandler)ruleTargetNextHandler).getNext();
            }
            if (ruleTargetNextHandler instanceof PathGlobHandler) {
                cleanUpPathGlobHandler((PathGlobHandler) ruleTargetNextHandler);
            }
        }
    }

    private void cleanUpPathGlobHandler(final PathGlobHandler pathGlobHandler) {
        pathGlobHandler.getPaths().forEach((k, poolHandler) -> {
            final ProxyHandler proxyHandler = ((PoolHandler) poolHandler).getProxyHandler();
            if (proxyHandler != null) {
                final ExtendedProxyClient proxyClient = (ExtendedProxyClient) proxyHandler.getProxyClient();
                proxyClient.removeAllHosts();
            }
        });
        pathGlobHandler.clear();
    }

    public synchronized void forceVirtualhostUpdate(String virtualhost) {
        if ("__ping__".equals(virtualhost)) return;
        if (nameVirtualHostHandler.getHosts().containsKey(virtualhost)) {
            logger.warn("[" + virtualhost + "] FORCING UPDATE");
            cleanUpNameVirtualHostHandler(virtualhost);
            nameVirtualHostHandler.removeHost(virtualhost);
        }
    }

    public synchronized void forcePoolUpdate(String poolName) {
        nameVirtualHostHandler.getHosts().entrySet().stream()
                .filter(e -> e.getValue() instanceof RuleTargetHandler).forEach(entryHost ->
        {
            final String virtualhost = entryHost.getKey();
            final HttpHandler handler = ((RuleTargetHandler)entryHost.getValue()).getNext();
            if (handler != null) {
                if (handler instanceof PathGlobHandler) {
                    forcePoolUpdateByPathGlobHandler(poolName, virtualhost, (PathGlobHandler) handler);
                }
                if (handler instanceof IPAddressAccessControlHandler) {
                    forcePoolUpdateByIpAclHandler(poolName, virtualhost, (IPAddressAccessControlHandler) handler);
                }
            }
        });
    }

    private synchronized void forcePoolUpdateByPathGlobHandler(String poolName, String virtualhost, PathGlobHandler handler) {
        handler.getPaths().entrySet().stream().map(Map.Entry::getValue)
                .filter(pathHandler -> pathHandler instanceof PoolHandler &&
                        ((PoolHandler) pathHandler).getPoolname() != null &&
                        ((PoolHandler) pathHandler).getPoolname().equals(poolName))
                .forEach(v -> forceVirtualhostUpdate(virtualhost));
    }

    private synchronized void forcePoolUpdateByIpAclHandler(String poolName, String virtualhost, IPAddressAccessControlHandler handler) {
        forcePoolUpdateByPathGlobHandler(poolName, virtualhost, (PathGlobHandler) handler.getNext());
    }

    public synchronized void forceAllUpdate() {
        nameVirtualHostHandler.getHosts().forEach((virtualhost, handler) -> forceVirtualhostUpdate(virtualhost));
    }
}
