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

import io.galeb.router.client.hostselectors.HostSelector;
import io.undertow.attribute.ExchangeAttribute;
import io.undertow.attribute.ExchangeAttributes;
import io.undertow.attribute.SubstituteEmptyWrapper;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.galeb.router.ResponseCodeOnError.Header.X_GALEB_ERROR;

@Component
public class AccessLogCompletionListener extends ProcessorLocalStatusCode implements ExchangeCompletionListener {

    private static final int MAX_REQUEST_TIME = Integer.MAX_VALUE - 1;
    private static final String REAL_DEST = "#REAL_DEST#";
    private static final String LOGPATTERN = "%a\t%v\t%r\t-\t-\tLocal:\t%s\t*-\t%B\t%D\tProxy:\t" +
                                             REAL_DEST +
                                             "\t%s\t-\t%b\t-\t-\tAgent:\t%{i,User-Agent}\tFwd:\t%{i,X-Forwarded-For}";

    private final Log logger = LogFactory.getLog(this.getClass());

    private final ExchangeAttribute tokens = ExchangeAttributes.parser(getClass().getClassLoader(), new SubstituteEmptyWrapper("-")).parse(LOGPATTERN);

    @Override
    public void exchangeEvent(HttpServerExchange exchange, NextListener nextListener) {
        try {
            final String tempRealDest = exchange.getAttachment(HostSelector.REAL_DEST);
            String realDest = extractUpstreamField(exchange.getResponseHeaders(), tempRealDest);
            String message = tokens.readAttribute(exchange);
            int realStatus = exchange.getStatusCode();
            long responseBytesSent = exchange.getResponseBytesSent();
            final Integer responseTime = Math.round(Float.parseFloat(responseTimeAttribute.readAttribute(exchange)));
            int fakeStatusCode = getFakeStatusCode(tempRealDest, realStatus, responseBytesSent, responseTime, MAX_REQUEST_TIME);
            if (fakeStatusCode != ProcessorLocalStatusCode.NOT_MODIFIED) {
                message = message.replaceAll("^(.*Local:\t)\\d{3}(\t.*Proxy:\t.*\t)\\d{3}(\t.*)$",
                        "$1" + String.valueOf(fakeStatusCode) + "$2" + String.valueOf(fakeStatusCode) + "$3");
            }
            Pattern compile = Pattern.compile("([^\\t]*\\t[^\\t]*\\t)([^\\t]+)(\\t.*)$");
            Matcher match = compile.matcher(message);
            if (match.find()) {
                message = match.group(1) + match.group(2).replace(" ", "\t") + match.group(3);
            }
            logger.info(message.replaceAll(REAL_DEST, realDest));
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            nextListener.proceed();
        }
    }
}
