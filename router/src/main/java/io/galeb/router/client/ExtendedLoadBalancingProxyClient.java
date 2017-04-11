/**
 * 
 */

package io.galeb.router.client;

import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.RoundRobinHostSelector;
import io.undertow.UndertowLogger;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientStatistics;
import io.undertow.client.UndertowClient;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.proxy.*;
import io.undertow.util.AttachmentKey;
import io.undertow.util.AttachmentList;
import io.undertow.util.CopyOnWriteMap;
import io.undertow.util.HttpString;
import org.xnio.OptionMap;
import org.xnio.ssl.XnioSsl;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import static io.undertow.server.handlers.proxy.ProxyConnectionPool.AvailabilityType.*;
import static org.xnio.IoUtils.safeClose;

public class ExtendedLoadBalancingProxyClient implements ProxyClient {

    private final AttachmentKey<ExclusiveConnectionHolder> exclusiveConnectionKey = AttachmentKey.create(ExclusiveConnectionHolder.class);

    private static final AttachmentKey<AttachmentList<Host>> ATTEMPTED_HOSTS = AttachmentKey.createList(Host.class);

    private volatile int problemServerRetry = 10;

    private final Set<String> sessionCookieNames = new CopyOnWriteArraySet<>();

    private volatile int connectionsPerThread = 10;
    private volatile int maxQueueSize = 0;
    private volatile int softMaxConnectionsPerThread = 5;
    private volatile int ttl = -1;

    private volatile Host[] hosts = {};

    private final HostSelector hostSelector;
    private final UndertowClient client;

    private final Map<String, Host> routes = new CopyOnWriteMap<>();

    private final ExclusivityChecker exclusivityChecker;

    private static final ProxyTarget PROXY_TARGET = new ProxyTarget() {
    };

    public ExtendedLoadBalancingProxyClient() {
        this(UndertowClient.getInstance());
    }

    public ExtendedLoadBalancingProxyClient(UndertowClient client) {
        this(client, null, null);
    }

    public ExtendedLoadBalancingProxyClient(ExclusivityChecker client) {
        this(UndertowClient.getInstance(), client, null);
    }

    public ExtendedLoadBalancingProxyClient(UndertowClient client, ExclusivityChecker exclusivityChecker) {
        this(client, exclusivityChecker, null);
    }

    public ExtendedLoadBalancingProxyClient(UndertowClient client, ExclusivityChecker exclusivityChecker, HostSelector hostSelector) {
        this.client = client;
        this.exclusivityChecker = exclusivityChecker;
        sessionCookieNames.add("JSESSIONID");
        if(hostSelector == null) {
            this.hostSelector = new RoundRobinHostSelector();
        } else {
            this.hostSelector = hostSelector;
        }
    }

    public ExtendedLoadBalancingProxyClient addSessionCookieName(final String sessionCookieName) {
        sessionCookieNames.add(sessionCookieName);
        return this;
    }

    public ExtendedLoadBalancingProxyClient removeSessionCookieName(final String sessionCookieName) {
        sessionCookieNames.remove(sessionCookieName);
        return this;
    }

    public ExtendedLoadBalancingProxyClient setProblemServerRetry(int problemServerRetry) {
        this.problemServerRetry = problemServerRetry;
        return this;
    }

    public int getProblemServerRetry() {
        return problemServerRetry;
    }

    public int getConnectionsPerThread() {
        return connectionsPerThread;
    }

    public ExtendedLoadBalancingProxyClient setConnectionsPerThread(int connectionsPerThread) {
        this.connectionsPerThread = connectionsPerThread;
        return this;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public ExtendedLoadBalancingProxyClient setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
        return this;
    }

    public ExtendedLoadBalancingProxyClient setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public ExtendedLoadBalancingProxyClient setSoftMaxConnectionsPerThread(int softMaxConnectionsPerThread) {
        this.softMaxConnectionsPerThread = softMaxConnectionsPerThread;
        return this;
    }

    public synchronized ExtendedLoadBalancingProxyClient addHost(final URI host) {
        return addHost(host, null, null);
    }

    public synchronized ExtendedLoadBalancingProxyClient addHost(final URI host, XnioSsl ssl) {
        return addHost(host, null, ssl);
    }

    public synchronized ExtendedLoadBalancingProxyClient addHost(final URI host, String jvmRoute) {
        return addHost(host, jvmRoute, null);
    }


    public synchronized ExtendedLoadBalancingProxyClient addHost(final URI host, String jvmRoute, XnioSsl ssl) {

        Host h = new Host(jvmRoute, null, host, ssl, OptionMap.EMPTY);
        Host[] existing = hosts;
        Host[] newHosts = new Host[existing.length + 1];
        System.arraycopy(existing, 0, newHosts, 0, existing.length);
        newHosts[existing.length] = h;
        this.hosts = newHosts;
        if (jvmRoute != null) {
            this.routes.put(jvmRoute, h);
        }
        return this;
    }


    public synchronized ExtendedLoadBalancingProxyClient addHost(final URI host, String jvmRoute, XnioSsl ssl, OptionMap options) {
        return addHost(null, host, jvmRoute, ssl, options);
    }


    public synchronized ExtendedLoadBalancingProxyClient addHost(final InetSocketAddress bindAddress, final URI host, String jvmRoute, XnioSsl ssl, OptionMap options) {
        Host h = new Host(jvmRoute, bindAddress, host, ssl, options);
        Host[] existing = hosts;
        Host[] newHosts = new Host[existing.length + 1];
        System.arraycopy(existing, 0, newHosts, 0, existing.length);
        newHosts[existing.length] = h;
        this.hosts = newHosts;
        if (jvmRoute != null) {
            this.routes.put(jvmRoute, h);
        }
        return this;
    }

    public synchronized ExtendedLoadBalancingProxyClient removeHost(final URI uri) {
        int found = -1;
        Host[] existing = hosts;
        Host removedHost = null;
        for (int i = 0; i < existing.length; ++i) {
            if (existing[i].uri.equals(uri)) {
                found = i;
                removedHost = existing[i];
                break;
            }
        }
        if (found == -1) {
            return this;
        }
        Host[] newHosts = new Host[existing.length - 1];
        System.arraycopy(existing, 0, newHosts, 0, found);
        System.arraycopy(existing, found + 1, newHosts, found, existing.length - found - 1);
        this.hosts = newHosts;
        removedHost.connectionPool.close();
        if (removedHost.jvmRoute != null) {
            routes.remove(removedHost.jvmRoute);
        }
        return this;
    }

    @Override
    public ProxyTarget findTarget(HttpServerExchange exchange) {
        return PROXY_TARGET;
    }

    @Override
    public void getConnection(ProxyTarget target, HttpServerExchange exchange, final ProxyCallback<ProxyConnection> callback, long timeout, TimeUnit timeUnit) {
        final ExclusiveConnectionHolder holder = exchange.getConnection().getAttachment(exclusiveConnectionKey);
        if (holder != null && holder.connection.getConnection().isOpen()) {
            // Something has already caused an exclusive connection to be allocated so keep using it.
            callback.completed(exchange, holder.connection);
            return;
        }

        final Host host = selectHost(exchange);
        if (host == null) {
            exchange.getResponseHeaders().add(HttpString.tryFromString("X-Error-Message"), "couldNotResolveBackend");
            callback.couldNotResolveBackend(exchange);
        } else {
            exchange.addToAttachmentList(ATTEMPTED_HOSTS, host);
            final URI uri = host.getUri();
            exchange.putAttachment(HostSelector.REAL_DEST, uri != null ? uri.toString() : "UNDEF");
            if (holder != null || (exclusivityChecker != null && exclusivityChecker.isExclusivityRequired(exchange))) {
                // If we have a holder, even if the connection was closed we now exclusivity was already requested so our client
                // may be assuming it still exists.
                host.connectionPool.connect(target, exchange, new ProxyConnectionProxyCallback()
                        .setHolder(holder).setCallback(callback).setTarget(target).setTimeout(timeout).setTimeUnit(timeUnit).setHost(host).setExclusive(true),
                        timeout, timeUnit, true);
            } else {
                host.connectionPool.connect(target, exchange, new ProxyConnectionProxyCallback()
                        .setCallback(callback).setTarget(target).setTimeout(timeout).setTimeUnit(timeUnit).setHost(host), timeout,
                        timeUnit, false);
            }
        }
    }

    private boolean tryNextHostIfFailed(final ProxyTarget target, final HttpServerExchange exchange, final ProxyCallback<ProxyConnection> callback, long timeout, TimeUnit timeUnit) {
        String uri = exchange.getAttachment(HostSelector.REAL_DEST);
        if (uri != null) {
            removeHost(URI.create(uri));
            exchange.removeAttachment(HostSelector.REAL_DEST);
            if (hosts.length > 0) {
                getConnection(target, exchange, callback, timeout, timeUnit);
                return true;
            }
        }
        return false;
    }

    protected Host selectHost(HttpServerExchange exchange) {
        AttachmentList<Host> attempted = exchange.getAttachment(ATTEMPTED_HOSTS);
        Host[] hosts = this.hosts;
        if (hosts.length == 0) {
            return null;
        }
        Host sticky = findStickyHost(exchange);
        if (sticky != null) {
            if(attempted == null || !attempted.contains(sticky)) {
                return sticky;
            }
        }
        int host = hostSelector.selectHost(hosts, exchange);

        final int startHost = host; //if the all hosts have problems we come back to this one
        Host full = null;
        Host problem = null;
        do {
            Host selected = hosts[host];
            if(attempted == null || !attempted.contains(selected)) {
                ProxyConnectionPool.AvailabilityType available = selected.connectionPool.available();
                if (available == AVAILABLE) {
                    return selected;
                } else if (available == FULL && full == null) {
                    full = selected;
                } else if ((available == PROBLEM || available == FULL_QUEUE) && problem == null) {
                    problem = selected;
                }
            }
            host = (host + 1) % hosts.length;
        } while (host != startHost);
        if (full != null) {
            return full;
        }
        if (problem != null) {
            return problem;
        }
        //no available hosts
        return null;
    }

    protected Host findStickyHost(HttpServerExchange exchange) {
        Map<String, Cookie> cookies = exchange.getRequestCookies();
        for (String cookieName : sessionCookieNames) {
            Cookie sk = cookies.get(cookieName);
            if (sk != null) {
                int index = sk.getValue().indexOf('.');

                if (index == -1) {
                    continue;
                }
                String route = sk.getValue().substring(index + 1);
                index = route.indexOf('.');
                if (index != -1) {
                    route = route.substring(0, index);
                }
                return routes.get(route);
            }
        }
        return null;
    }

    public boolean isHostsEmpty() {
        return hosts.length == 0;
    }

    public final class Host extends ConnectionPoolErrorHandler.SimpleConnectionPoolErrorHandler implements ConnectionPoolManager {
        final ProxyConnectionPool connectionPool;
        final String jvmRoute;
        final URI uri;
        final XnioSsl ssl;

        private Host(String jvmRoute, InetSocketAddress bindAddress, URI uri, XnioSsl ssl, OptionMap options) {
            this.connectionPool = new ProxyConnectionPool(this, bindAddress, uri, ssl, client, options);
            this.jvmRoute = jvmRoute;
            this.uri = uri;
            this.ssl = ssl;
        }

        @Override
        public int getProblemServerRetry() {
            return problemServerRetry;
        }

        @Override
        public int getMaxConnections() {
            return connectionsPerThread;
        }

        @Override
        public int getMaxCachedConnections() {
            return connectionsPerThread;
        }

        @Override
        public int getSMaxConnections() {
            return softMaxConnectionsPerThread;
        }

        @Override
        public long getTtl() {
            return ttl;
        }

        @Override
        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public URI getUri() {
            return uri;
        }

        public int getOpenConnection() {
            return connectionPool.getOpenConnections();
        }

        public ClientStatistics getClientStatistics() {
            return connectionPool.getClientStatistics();
        }
    }

    private static class ExclusiveConnectionHolder {

        private ProxyConnection connection;

    }

    private class ProxyConnectionProxyCallback implements ProxyCallback<ProxyConnection> {

        private ExclusiveConnectionHolder holder = null;
        private ProxyCallback<ProxyConnection> callback;
        private ProxyTarget target;
        private long timeout;
        private TimeUnit timeUnit;
        private Host host;
        private boolean exclusive = false;

        @Override
        public void completed(HttpServerExchange exchange, ProxyConnection result) {
            if (exclusive) {
                if (holder != null) {
                    holder.connection = result;
                } else {
                    final ExclusiveConnectionHolder newHolder = new ExclusiveConnectionHolder();
                    newHolder.connection = result;
                    ServerConnection connection = exchange.getConnection();
                    connection.putAttachment(exclusiveConnectionKey, newHolder);
                    connection.addCloseListener(closeListener -> {
                        ClientConnection clientConnection = newHolder.connection.getConnection();
                        if (clientConnection.isOpen()) {
                            safeClose(clientConnection);
                        }
                    });
                }
            }
            callback.completed(exchange, result);
        }

        @Override
        public void queuedRequestFailed(HttpServerExchange exchange) {
            exchange.removeAttachment(HostSelector.REAL_DEST);
            exchange.getResponseHeaders().add(HttpString.tryFromString("X-Error-Message"), "queuedRequestFailed");
            callback.queuedRequestFailed(exchange);
        }

        @Override
        public void failed(HttpServerExchange exchange) {
            if (tryNextHostIfFailed(target, exchange, callback, timeout, timeUnit)) return;
            UndertowLogger.PROXY_REQUEST_LOGGER.proxyFailedToConnectToBackend(exchange.getRequestURI(), host.uri);
            callback.failed(exchange);
        }

        @Override
        public void couldNotResolveBackend(HttpServerExchange exchange) {
            exchange.removeAttachment(HostSelector.REAL_DEST);
            exchange.getResponseHeaders().add(HttpString.tryFromString("X-Error-Message"), "couldNotResolveBackend");
            callback.couldNotResolveBackend(exchange);
        }
        
        public ProxyConnectionProxyCallback setHolder(final ExclusiveConnectionHolder holder) {
            this.holder = holder;
            return this;
        }

        public ProxyConnectionProxyCallback setCallback(final ProxyCallback<ProxyConnection> callback) {
            this.callback = callback;
            return this;
        }

        public ProxyConnectionProxyCallback setTarget(final ProxyTarget target) {
            this.target = target;
            return this;
        }

        public ProxyConnectionProxyCallback setTimeout(final long timeout) {
            this.timeout = timeout;
            return this;
        }

        public ProxyConnectionProxyCallback setTimeUnit(final TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public ProxyConnectionProxyCallback setHost(final Host host) {
            this.host = host;
            return this;
        }

        public ProxyConnectionProxyCallback setExclusive(final boolean exclusive) {
            this.exclusive = exclusive;
            return this;
        }

    }
}
