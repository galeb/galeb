package io.galeb.core.entity;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;

@Entity
public class Pool extends AbstractEntity implements WithStatus {

    @ManyToMany
    private Set<Rule> rules;

    @ManyToMany
    private Set<Environment> environments;

    @ManyToMany
    private Set<Target> targets;

    @ManyToOne
    private Project project;

    @ManyToOne
    private BalancePolicy balancePolicy;

    @Column(name = "name", nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    // Healthcheck Attributes

    @Column(name = "hcPath")
    private String hcPath;

    @Column(name = "hcHttpStatusCode")
    private String hcHttpStatusCode;

    @Column(name = "hcHost")
    private String hcHost;

    @Column(name = "hcTcpOnly", nullable = false)
    private Boolean hcTcpOnly;

    @Column(name = "hcHttpMethod")
    private HealthCheck.HttpMethod hcHttpMethod;

    @Column(name = "hcBody")
    private String hcBody;

    @ElementCollection
    @JoinColumn
    private Map<String, String> hcHeaders;

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    public Set<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<Environment> environments) {
        this.environments = environments;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        this.targets = targets;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public BalancePolicy getBalancePolicy() {
        return balancePolicy;
    }

    public void setBalancePolicy(BalancePolicy balancePolicy) {
        this.balancePolicy = balancePolicy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHcPath() {
        return hcPath;
    }

    public void setHcPath(String hcPath) {
        this.hcPath = hcPath;
    }

    public String getHcHttpStatusCode() {
        return hcHttpStatusCode;
    }

    public void setHcHttpStatusCode(String hcHttpStatusCode) {
        this.hcHttpStatusCode = hcHttpStatusCode;
    }

    public String getHcHost() {
        return hcHost;
    }

    public void setHcHost(String hcHost) {
        this.hcHost = hcHost;
    }

    public Boolean getHcTcpOnly() {
        return hcTcpOnly;
    }

    public void setHcTcpOnly(Boolean hcTcpOnly) {
        this.hcTcpOnly = hcTcpOnly;
    }

    public HealthCheck.HttpMethod getHcHttpMethod() {
        return hcHttpMethod;
    }

    public void setHcHttpMethod(HealthCheck.HttpMethod hcHttpMethod) {
        this.hcHttpMethod = hcHttpMethod;
    }

    public String getHcBody() {
        return hcBody;
    }

    public void setHcBody(String hcBody) {
        this.hcBody = hcBody;
    }

    public Map<String, String> getHcHeaders() {
        return hcHeaders;
    }

    public void setHcHeaders(Map<String, String> hcHeaders) {
        this.hcHeaders = hcHeaders;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
