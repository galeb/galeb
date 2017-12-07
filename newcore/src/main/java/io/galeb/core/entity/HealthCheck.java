package io.galeb.core.entity;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import java.util.Map;

@Entity
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

    @Column(name = "path")
    private String path;

    @Column(name = "httpStatusCode")
    private String httpStatusCode;

    @Column(name = "tcpOnly", nullable = false)
    private Boolean tcpOnly;

    @Column(name = "httpMethod")
    private HttpMethod httpMethod;

    @Column(name = "body")
    private String body;

    @ElementCollection
    @JoinColumn
    private Map<String, String> headers;

    @Column(name = "name", nullable = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
