package io.galeb.api.log;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.JsonHttpLogFormatter;
import org.zalando.logbook.Precorrelation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

public class PrincipalHttpLogFormatter implements HttpLogFormatter {

    private final JsonHttpLogFormatter delegate;

    public PrincipalHttpLogFormatter(final JsonHttpLogFormatter delegate) {
        this.delegate = delegate;
    }

    @Override
    public String format(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        final Map<String, Object> content = delegate.prepare(precorrelation);
        content.put("principal", getPrincipal());
        return delegate.format(content);
    }

    @Override
    public String format(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        final Map<String, Object> content = delegate.prepare(correlation);
        content.put("principal", getPrincipal());
        return delegate.format(content);
    }

    private String getPrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        @Nullable final String principal = authentication.getName();
        return principal == null ? "anonymous" : principal;
    }
}