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

package io.galeb.router.handlers.completionListeners;

import static io.undertow.attribute.ExchangeAttributes.localServerName;
import static io.undertow.attribute.ExchangeAttributes.responseCode;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.handlers.PathGlobHandler;
import io.galeb.router.handlers.PoolHandler;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;

@Component
public class PrometheusCompletionListener extends ProcessorLocalStatusCode implements ExchangeCompletionListener {

    static final Counter requestCounter = Counter.build().name("galeb_http_requests_total")
            .labelNames("virtualhost", "pool", "rule", "status").help("Total requests.").register();

    static final Counter errorCounter = Counter.build().name("galeb_errors_total")
            .labelNames("virtualhost", "pool", "rule", "error").help("Total errors.").register();

    static final Histogram latencyHistogram = Histogram.build().name("galeb_http_requests")
            .labelNames("virtualhost", "pool", "rule", "status").help("Galeb backend latency")
            .exponentialBuckets(0.05, 4, 6).register();

    private final Log logger = LogFactory.getLog(this.getClass());

    private static final int MAX_REQUEST_TIME = Integer.MAX_VALUE - 1;

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        try {
            final String virtualHost = localServerName().readAttribute(exchange);
            final String pool = isEmpty(exchange.getAttachment(PoolHandler.POOL_NAME));
            final String rule = isEmpty(exchange.getAttachment(PathGlobHandler.RULE_NAME));

            final int originalStatusCode = Integer.parseInt(responseCode().readAttribute(exchange));
            final String realDestAttached = exchange.getAttachment(HostSelector.REAL_DEST);
            final long responseBytesSent = exchange.getResponseBytesSent();
            final float responseTime = Float.parseFloat(responseTimeAttribute.readAttribute(exchange));
            final Integer roundTime = Math.round(responseTime);
            final int fakeStatusCode = getFakeStatusCode(realDestAttached, originalStatusCode, responseBytesSent,
                    roundTime, MAX_REQUEST_TIME);

            requestCounter.labels(virtualHost, pool, rule, reportStatus(originalStatusCode)).inc();
            latencyHistogram.labels(virtualHost, pool, rule, reportStatus(originalStatusCode)).observe(responseTime);

            if (fakeStatusCode != ProcessorLocalStatusCode.NOT_MODIFIED) {
                errorCounter.labels(virtualHost, pool, rule, extractXGalebErrorHeader(exchange.getResponseHeaders()))
                        .inc();
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            nextListener.proceed();
        }
    }

    private String isEmpty(String v) {
        return v != null ? v : "empty";
    }

    private String reportStatus(int status) {
        if (status < 200) {
            return "1xx";
        } else if (status < 300) {
            return "2xx";
        } else if (status < 400) {
            return "3xx";
        } else if (status < 500) {
            return "4xx";
        }
        return "5xx";
    }
}
