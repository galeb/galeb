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

package io.galeb.router.services;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import io.galeb.core.enums.SystemEnv;
import io.undertow.Undertow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToLongFunction;

@Service
@Order(10)
public class JmxReporterService {

    public static final String MBEAN_DOMAIN = JmxReporterService.class.getName();

    private final AtomicLong lastRequestCount = new AtomicLong(0L);
    private final AtomicLong lastBytesReceived = new AtomicLong(0L);
    private final AtomicLong lastBytesSent = new AtomicLong(0L);
    private final Undertow undertow;

    @Autowired
    public JmxReporterService(final Undertow undertow) {
        this.undertow = undertow;
    }

    @Bean
    public JmxReporter jmxReporter() {
        final MetricRegistry register = new MetricRegistry();
        register.register("ActiveConnections", (Gauge<Long>) () -> undertow.getListenerInfo().stream()
                .mapToLong(l -> l.getConnectorStatistics().getActiveConnections()).sum());
        register.register("ActiveRequests", (Gauge<Long>) () -> undertow.getListenerInfo().stream()
                .mapToLong(l -> l.getConnectorStatistics().getActiveRequests()).sum());
        register.register("RequestCount", (Gauge<Long>) this::getRequestCount);
        register.register("BytesReceived", (Gauge<Long>) this::getBytesReceived);
        register.register("BytesSent", (Gauge<Long>) this::getBytesSent);
        final JmxReporter jmxReporter = JmxReporter.forRegistry(register).inDomain(MBEAN_DOMAIN).build();
        if (Boolean.parseBoolean(SystemEnv.ENABLE_UNDERTOW_JMX.getValue())) {
            jmxReporter.start();
        }
        return jmxReporter;
    }

    private long extractDelta(final AtomicLong last, final ToLongFunction<Undertow.ListenerInfo> longFunction) {
        long start = System.nanoTime();
        double localLast = last.get() * 1.0;
        double current = undertow.getListenerInfo().stream().mapToLong(longFunction).sum() * 1.0;
        long end = System.nanoTime();
        last.set((long) current);
        return Math.round((current * ((double) end / (double) start)) - localLast);
    }

    private long getRequestCount() {
        return extractDelta(lastRequestCount, l -> l.getConnectorStatistics().getRequestCount());
    }

    private long getBytesReceived() {
        return extractDelta(lastBytesReceived, l -> l.getConnectorStatistics().getBytesReceived());
    }

    private long getBytesSent() {
        return extractDelta(lastBytesSent, l -> l.getConnectorStatistics().getBytesSent());
    }
}
