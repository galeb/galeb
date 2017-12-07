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

    @Column
    private String path;

    @Column
    private String httpStatusCode;

    @Column(nullable = false)
    private Boolean tcpOnly;

    @Column
    private HttpMethod httpMethod;

    @Column
    private String body;

    @ElementCollection
    @JoinColumn
    private Map<String, String> headers;

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
        this.headers = headers;
    }
}
