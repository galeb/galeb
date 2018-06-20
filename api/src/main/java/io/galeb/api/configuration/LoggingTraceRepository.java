package io.galeb.api.configuration;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.actuate.trace.InMemoryTraceRepository;
import org.springframework.boot.actuate.trace.Trace;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LoggingTraceRepository implements TraceRepository {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(LoggingTraceRepository.class);

    private final TraceRepository delegate = new InMemoryTraceRepository();
    private final Gson gson = new Gson();

    @Override
    public List<Trace> findAll() {
        return delegate.findAll();
    }

    @Override
    public void add(Map<String, Object> traceInfo) {
        LOGGER.info(gson.toJson(traceInfo));
        this.delegate.add(traceInfo);
    }
}