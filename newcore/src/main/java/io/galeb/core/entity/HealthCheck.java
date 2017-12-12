package io.galeb.core.entity;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "healthcheck", uniqueConstraints = { @UniqueConstraint(name = "UK_healthcheck_name", columnNames = { "name" }) })
public class HealthCheck extends AbstractEntity {

    @SuppressWarnings("unused")
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

    private Boolean tcpOnly = false;

    private HttpMethod httpMethod;

    private String body;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Map<String, String> headers = new HashMap<>();

    @Column(nullable = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public Boolean getTcpOnly() {
        return tcpOnly;
    }

    public void setTcpOnly(Boolean tcpOnly) {
        this.tcpOnly = tcpOnly;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers.clear();
            this.headers.putAll(headers);
        }
    }
}
