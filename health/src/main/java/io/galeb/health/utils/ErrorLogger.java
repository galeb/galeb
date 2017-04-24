package io.galeb.health.utils;

import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ErrorLogger {

    private static final StackTraceElement UNDEF_STASK_TRACE_ELEMENT =
            new StackTraceElement(Object.class.getName(), "undef", "undef", -1);

    public static void logError(Exception e, Class klazz) {
        String message = e.getMessage();
        if (message == null) {
            message = e.getClass().getSimpleName();
        }
        LoggerFactory.getLogger(klazz).error("Line " + Arrays.stream(e.getStackTrace())
                                                             .filter(stacktrace -> stacktrace.getClassName().equals(klazz.getName()))
                                                             .findFirst().orElse(UNDEF_STASK_TRACE_ELEMENT)
                                                             .getLineNumber() + ": " + message);
    }
}
