package io.galeb.router.completionListeners;

import io.galeb.router.client.hostselectors.HostSelector;
import io.undertow.attribute.ExchangeAttribute;
import io.undertow.attribute.ExchangeAttributes;
import io.undertow.attribute.SubstituteEmptyWrapper;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AccessLogCompletionListener implements ExchangeCompletionListener, ProcessorLocalStatusCode {

    private static final int MAX_REQUEST_TIME = Integer.MAX_VALUE - 1;
    private static final String UNKNOWN = "UNKNOWN";
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
            String realDest = tempRealDest != null ? tempRealDest : UNKNOWN;
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
