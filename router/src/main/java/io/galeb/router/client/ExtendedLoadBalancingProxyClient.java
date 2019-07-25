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

package io.galeb.router.client;

import io.galeb.router.ResponseCodeOnError;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.RoundRobinHostSelector;
import io.undertow.UndertowLogger;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientStatistics;
import io.undertow.client.UndertowClient;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.proxy.ConnectionPoolErrorHandler;
import io.undertow.server.handlers.proxy.ConnectionPoolManager;
import io.undertow.server.handlers.proxy.ExclusivityChecker;
import io.undertow.server.handlers.proxy.ProxyCallback;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyConnection;
import io.undertow.server.handlers.proxy.ProxyConnectionPool;
import io.undertow.server.handlers.proxy.RouteIteratorFactory;
import io.undertow.server.handlers.proxy.RouteIteratorFactory.ParsingCompatibility;
import io.undertow.server.handlers.proxy.RouteParsingStrategy;
import io.undertow.util.AttachmentKey;
import io.undertow.util.AttachmentList;
import io.undertow.util.CopyOnWriteMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xnio.OptionMap;
import org.xnio.ssl.XnioSsl;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import static io.undertow.server.handlers.proxy.ProxyConnectionPool.AvailabilityType.AVAILABLE;
import static io.undertow.server.handlers.proxy.ProxyConnectionPool.AvailabilityType.FULL;
import static io.undertow.server.handlers.proxy.ProxyConnectionPool.AvailabilityType.FULL_QUEUE;
import static io.undertow.server.handlers.proxy.ProxyConnectionPool.AvailabilityType.PROBLEM;
import static org.xnio.IoUtils.safeClose;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused", "SameParameterValue"})
public class ExtendedLoadBalancingProxyClient implements ProxyClient, ExtendedProxyClient {

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * The attachment key that is used to attach the proxy connection to the exchange.
     * <p>
     * This cannot be static as otherwise a connection from a different client could be re-used.
     */
    private final AttachmentKey<ExclusiveConnectionHolder> exclusiveConnectionKey = AttachmentKey.create(ExclusiveConnectionHolder.class);

    private static final AttachmentKey<AttachmentList<Host>> ATTEMPTED_HOSTS = AttachmentKey.createList(Host.class);

    /**
     * Time in seconds between retries for problem servers
     */
    private volatile int problemServerRetry = 10; // seconds

    private final Set<String> sessionCookieNames = new CopyOnWriteArraySet<>();

    /**
     * The number of connections to create per thread
     */
    private volatile int connectionsPerThread = 10;
    private volatile int maxQueueSize = 0;
    private volatile int softMaxConnectionsPerThread = 5;
    private volatile int ttl = -1;

    /**
     * The hosts list.
     */
    private volatile Host[] hosts = {};

    private final HostSelector hostSelector;
    private final UndertowClient client;

    private final Map<String, Host> routes = new CopyOnWriteMap<>();
    private RouteIteratorFactory routeIteratorFactory = new RouteIteratorFactory(RouteParsingStrategy.SINGLE, ParsingCompatibility.MOD_JK);

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

    public ExtendedLoadBalancingProxyClient setRankedRoutingDelimiter(String rankedRoutingDelimiter) {
        this.routeIteratorFactory = new RouteIteratorFactory(RouteParsingStrategy.RANKED, ParsingCompatibility.MOD_JK, rankedRoutingDelimiter);
        return this;
    }


    public synchronized ExtendedLoadBalancingProxyClient addHost(final URI host) {
        return addHost(host, null, null);
    }
    
    public synchronized ExtendedLoadBalancingProxyClient addHost(final URI host, OptionMap options) {
        return addHost(null, host, null, null, options);
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
        sortHosts(newHosts);
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
        sortHosts(newHosts);
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
            exchange.getResponseHeaders().add(ResponseCodeOnError.Header.X_GALEB_ERROR, ResponseCodeOnError.COULD_NOT_RESOLVE_BACKEND.getMessage());
            callback.couldNotResolveBackend(exchange);
        } else {
            exchange.addToAttachmentList(ATTEMPTED_HOSTS, host);
            final URI uri = host.getUri();
            exchange.putAttachment(HostSelector.REAL_DEST, uri != null ? uri.toString() : "UNDEF");
            if (holder != null || (exclusivityChecker != null && exclusivityChecker.isExclusivityRequired(exchange))) {
                // If we have a holder, even if the connection was closed we now exclusivity was already requested so our client
                // may be assuming it still exists.
                host.connectionPool.connect(target, exchange, new ProxyConnectionProxyCallbackExclusive()
                        .setHolder(holder).setCallback(callback).setHost(host), timeout, timeUnit, true);
            } else {
                host.connectionPool.connect(target, exchange, new ProxyConnectionProxyCallback()
                        .setCallback(callback).setHost(host), timeout, timeUnit, false);
            }
        }
    }

    protected Host selectHost(HttpServerExchange exchange) {
        AttachmentList<Host> attempted = exchange.getAttachment(ATTEMPTED_HOSTS);
        Host[] hosts = this.hosts;
        if (hosts.length == 0) {
            return null;
        }

        Iterator<CharSequence> parsedRoutes = parseRoutes(exchange);
        while (parsedRoutes.hasNext()) {
            // Attempt to find the first existing host which was not yet attempted
            Host host = this.routes.get(parsedRoutes.next().toString());
            if (host != null) {
                if (attempted == null || !attempted.contains(host)) {
                    return host;
                }
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
                    if (logger.isDebugEnabled()) {
                        logger.debug("Host " + selected.getUri().toString() + " ignored: PROBLEM (scheduled to retry after " + problemServerRetry + " seconds)");
                    }
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

    protected Iterator<CharSequence> parseRoutes(HttpServerExchange exchange) {
        Map<String, Cookie> cookies = exchange.getRequestCookies();
        for (String cookieName : sessionCookieNames) {
            Cookie sessionCookie = cookies.get(cookieName);
            if (sessionCookie != null) {
                return routeIteratorFactory.iterator(sessionCookie.getValue());
            }
        }
        return routeIteratorFactory.iterator(null);
    }

    private void sortHosts(final Host[] newHosts) {
        Arrays.sort(newHosts, Host::compareTo);
    }

    @Override
    public boolean isHostsEmpty() {
        return hosts.length == 0;
    }

    @Override
    public synchronized void removeAllHosts() {
        Arrays.stream(hosts).map(Host::getUri).forEach(this::removeHost);
    }

    public class Host extends ConnectionPoolErrorHandler.SimpleConnectionPoolErrorHandler implements ConnectionPoolManager, Comparable<Host> {
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

        @Override
        public int compareTo(Host other) {
            return uri.compareTo(other.getUri());
        }
    }

    private static class ExclusiveConnectionHolder {

        private ProxyConnection connection;

    }

    private class ProxyConnectionProxyCallbackExclusive extends ProxyConnectionProxyCallback {

        private ExclusiveConnectionHolder holder = null;

        @Override
        public void completed(HttpServerExchange exchange, ProxyConnection proxyConnection) {
            if (holder != null) {
                holder.connection = proxyConnection;
            } else {
                final ExclusiveConnectionHolder newHolder = new ExclusiveConnectionHolder();
                newHolder.connection = proxyConnection;
                ServerConnection connection = exchange.getConnection();
                connection.putAttachment(exclusiveConnectionKey, newHolder);
                connection.addCloseListener(closeListener -> {
                    ClientConnection clientConnection = newHolder.connection.getConnection();
                    if (clientConnection.isOpen()) {
                        safeClose(clientConnection);
                    }
                });
            }
            callback.completed(exchange, proxyConnection);
        }

        public ProxyConnectionProxyCallbackExclusive setHolder(final ExclusiveConnectionHolder holder) {
            this.holder = holder;
            return this;
        }
    }

    private class ProxyConnectionProxyCallback implements ProxyCallback<ProxyConnection> {

        protected ProxyCallback<ProxyConnection> callback;
        protected Host host;

        @Override
        public void queuedRequestFailed(HttpServerExchange exchange) {
            exchange.removeAttachment(HostSelector.REAL_DEST);
            exchange.getResponseHeaders().add(ResponseCodeOnError.Header.X_GALEB_ERROR, ResponseCodeOnError.QUEUED_REQUEST_FAILED.getMessage());
            callback.queuedRequestFailed(exchange);
        }

        @Override
        public void completed(HttpServerExchange exchange, ProxyConnection proxyConnection) {
            callback.completed(exchange, proxyConnection);
        }

        @Override
        public void failed(HttpServerExchange exchange) {
            exchange.removeAttachment(HostSelector.REAL_DEST);
            UndertowLogger.PROXY_REQUEST_LOGGER.proxyFailedToConnectToBackend(exchange.getRequestURI(), host.uri);
            callback.failed(exchange);
        }

        @Override
        public void couldNotResolveBackend(HttpServerExchange exchange) {
            exchange.removeAttachment(HostSelector.REAL_DEST);
            exchange.getResponseHeaders().add(ResponseCodeOnError.Header.X_GALEB_ERROR, ResponseCodeOnError.COULD_NOT_RESOLVE_BACKEND.getMessage());
            callback.couldNotResolveBackend(exchange);
        }

        public ProxyConnectionProxyCallback setCallback(final ProxyCallback<ProxyConnection> callback) {
            this.callback = callback;
            return this;
        }

        public ProxyConnectionProxyCallback setHost(final Host host) {
            this.host = host;
            return this;
        }

    }
}
