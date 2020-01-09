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

import io.galeb.core.enums.SystemEnv;
import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.handlers.RequestIDHandler;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import static io.undertow.attribute.ExchangeAttributes.*;

@Component
public class AccessLogCompletionListener extends ProcessorLocalStatusCode implements ExchangeCompletionListener {

    private static final int MAX_REQUEST_TIME = Integer.MAX_VALUE - 1;
    private static final String REQUESTID_HEADER = SystemEnv.REQUESTID_HEADER.getValue();
    private static final String TAB = "\t";

    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        try {
            final String remoteAddr = remoteIp().readAttribute(exchange); // %a
            final String host = localServerName().readAttribute(exchange); // %v
            final String requestElements[] = requestList().readAttribute(exchange).split(" "); // %r
            final String method = exchange.getRequestMethod().toString();
            final String requestUri = exchange.getRequestURI();
            final String proto = exchange.getProtocol().toString();
            final String refer = requestElements.length > 3 ? requestElements[3] : null;
            final String xMobileGroup = requestElements.length > 4 ? requestElements[4] : null;
            final int originalStatusCode = Integer.parseInt(responseCode().readAttribute(exchange)); // %s
            final long responseBytesSent = exchange.getResponseBytesSent();
            final String bytesSent = Long.toString(responseBytesSent); // %B
            final String bytesSentOrDash = responseBytesSent == 0L ? "-" : bytesSent; // %b
            final Integer responseTime = Math.round(Float.parseFloat(responseTimeAttribute.readAttribute(exchange))); // %D
            final String realDestAttached = exchange.getAttachment(HostSelector.REAL_DEST);
            final String realDest = realDestAttached != null ? realDestAttached : extractXGalebErrorHeader(exchange.getResponseHeaders());
            final String userAgent = requestHeader(Headers.USER_AGENT).readAttribute(exchange); // %{i,User-Agent}
            final String requestId = !"".equals(REQUESTID_HEADER) ? requestHeader(RequestIDHandler.requestIdHeader()).readAttribute(exchange) : null; // %{i,?REQUEST_ID?}
            final String xForwardedFor = requestHeader(Headers.X_FORWARDED_FOR).readAttribute(exchange); // %{i,X-Forwarded-For}

            final int fakeStatusCode = getFakeStatusCode(realDestAttached, originalStatusCode, responseBytesSent, responseTime, MAX_REQUEST_TIME);
            final int statusCode = fakeStatusCode != ProcessorLocalStatusCode.NOT_MODIFIED ? fakeStatusCode : originalStatusCode;

            final String message =
                    remoteAddr + TAB + host + TAB + method + TAB + requestUri + TAB + proto +
                    TAB + (refer != null ? refer : "-") + TAB + (xMobileGroup != null ? xMobileGroup : "-") +
                    TAB + "Local:" + TAB + statusCode + TAB + "*-" +
                    TAB + bytesSent + TAB + responseTime + TAB + "Proxy:" + TAB + realDest +
                    TAB + statusCode + TAB + "-" + TAB + bytesSentOrDash +
                    TAB + "-" + TAB + "-" + TAB + "Agent:" + TAB + (userAgent != null ? userAgent : "-") +
                    (requestId != null ? TAB + requestId : "-") +
                    TAB + "Fwd:" + TAB + (xForwardedFor != null ? xForwardedFor : "-") +
                    TAB + "SSL:" + TAB + "-" + TAB + "-" + TAB + "-" + TAB + "-";

            logger.info(message);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            nextListener.proceed();
        }
    }
}
