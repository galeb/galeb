package io.galeb.router.handlers;

import io.galeb.router.completionListeners.AccessLogCompletionListener;
import io.galeb.router.completionListeners.StatsdCompletionListener;
import io.galeb.router.configurations.RootHandlerConfiguration;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.IPAddressAccessControlHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class RootHandler implements HttpHandler {

    private static final int SERVICE_UNAVAILABLE = 503;

    private final Log logger = LogFactory.getLog(this.getClass());

    private final NameVirtualHostHandler nameVirtualHostHandler;
    private final AccessLogCompletionListener accessLogCompletionListener;
    private final StatsdCompletionListener statsdCompletionListener;

    private boolean enableAccessLog = false; // TODO: property
    private boolean enableStatsd    = true;  // TODO: property

    public RootHandler(final NameVirtualHostHandler nameVirtualHostHandler,
                       final AccessLogCompletionListener accessLogCompletionListener,
                       final StatsdCompletionListener statsdCompletionListener) {

        this.nameVirtualHostHandler = nameVirtualHostHandler;
        this.accessLogCompletionListener = accessLogCompletionListener;
        this.statsdCompletionListener = statsdCompletionListener;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            if (enableAccessLog) exchange.addExchangeCompleteListener(accessLogCompletionListener);
            if (enableStatsd) exchange.addExchangeCompleteListener(statsdCompletionListener);

            nameVirtualHostHandler.handleRequest(exchange);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            exchange.setStatusCode(SERVICE_UNAVAILABLE);
        }
    }

    public synchronized void forceAllUpdate() {
        nameVirtualHostHandler.getHosts().forEach((virtualhost, handler) -> forceVirtualhostUpdate(virtualhost));
    }

    public synchronized void forceVirtualhostUpdate(String virtualhost) {
        if ("__ping__".equals(virtualhost)) return;
        logger.warn("[" + virtualhost + "] FORCING UPDATE");
        nameVirtualHostHandler.removeHost(virtualhost);
        ((NameVirtualHostDefaultHandler)nameVirtualHostHandler.getDefaultHandler()).forgetIt(virtualhost);
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

    private void forcePoolUpdateByPathGlobHandler(String poolName, String virtualhost, PathGlobHandler handler) {
        handler.getPaths().entrySet().stream().map(Map.Entry::getValue)
                .filter(pathHandler -> pathHandler instanceof PoolHandler &&
                        ((PoolHandler) pathHandler).getPoolname() != null &&
                        ((PoolHandler) pathHandler).getPoolname().equals(poolName))
                .forEach(v -> forceVirtualhostUpdate(virtualhost));
    }

    private void forcePoolUpdateByIpAclHandler(String poolName, String virtualhost, IPAddressAccessControlHandler handler) {
        forcePoolUpdateByPathGlobHandler(poolName, virtualhost, (PathGlobHandler) handler.getNext());
    }
}
