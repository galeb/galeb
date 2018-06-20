package io.galeb.api.configuration;

import org.springframework.boot.actuate.trace.TraceProperties;
import org.springframework.boot.actuate.trace.WebRequestTraceFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CustomWebTraceFilter extends WebRequestTraceFilter {

    public CustomWebTraceFilter(LoggingTraceRepository repository, TraceProperties properties) {
        super(repository, properties);
        Set<TraceProperties.Include> setIncludes = Stream.of(TraceProperties.Include.values()).collect(Collectors.toCollection(HashSet::new));
        properties.setInclude(setIncludes);
    }

    @Override
    protected Map<String, Object> getTrace(HttpServletRequest request) {
        Map<String, Object> trace = super.getTrace(request);
        String body = "";
        try {
            Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            body = s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        trace.put("payload", body);
        return trace;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void enhanceTrace(Map<String, Object> trace, HttpServletResponse response) {
        trace.put("status", response.getStatus());
        super.enhanceTrace(trace, response);
    }

}
