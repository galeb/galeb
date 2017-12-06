package io.galeb.core.entity;

import java.util.Map;

public class HealthCheck extends AbstractEntity {

    public enum HttpMethod {
        GET,
        POST,
        OPTIONS,
        PUT,
        HEAD,
        DELETE,
        CONNECT,
        TRACE,
        PATCH
    }

    private String path;
    private String httpStatusCode;
    private Boolean tcpOnly;
    private HttpMethod httpMethod;
    private String body;
    private Map<String, String> headers;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
