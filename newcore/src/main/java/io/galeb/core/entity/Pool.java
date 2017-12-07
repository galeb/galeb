package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_name_pool", columnNames = { "name" }) })
public class Pool extends AbstractEntity implements WithStatus {

    @ManyToMany
    private Set<Rule> rules= new HashSet<>();

    @ManyToMany
    private Set<Environment> environments = new HashSet<>();

    @ManyToMany
    private Set<Target> targets = new HashSet<>();

    @ManyToOne
    private Project project;

    @ManyToOne
    private BalancePolicy balancePolicy;

    @Column(nullable = false)
    private String name;

    @Transient
    private Status status = Status.UNKNOWN;

    // Healthcheck Attributes

    private String hcPath;

    private String hcHttpStatusCode;

    private String hcHost;

    private Boolean hcTcpOnly = false;

    private HealthCheck.HttpMethod hcHttpMethod = HealthCheck.HttpMethod.GET;

    private String hcBody;

    @ElementCollection
    @JoinColumn(nullable = false)
    private Map<String, String> hcHeaders = new HashMap<>();

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        if (rules != null) {
            this.rules.clear();
            this.rules.addAll(rules);
        }
    }

    public Set<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<Environment> environments) {
        if (environments != null) {
            this.environments.clear();
            this.environments.addAll(environments);
        }
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        if (targets != null) {
            this.targets.clear();
            this.targets.addAll(targets);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        Assert.notNull(project, "Project is NULL");
        this.project = project;
    }

    public BalancePolicy getBalancePolicy() {
        return balancePolicy;
    }

    public void setBalancePolicy(BalancePolicy balancePolicy) {
        Assert.notNull(balancePolicy, "BalancePolicy is NULL");
        this.balancePolicy = balancePolicy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
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
        if (hcTcpOnly != null) {
            this.hcTcpOnly = hcTcpOnly;
        }
    }

    public HealthCheck.HttpMethod getHcHttpMethod() {
        return hcHttpMethod;
    }

    public void setHcHttpMethod(HealthCheck.HttpMethod hcHttpMethod) {
        if (hcHttpMethod != null) {
            this.hcHttpMethod = hcHttpMethod;
        }
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
        if (hcHeaders != null) {
            this.hcHeaders.clear();
            this.hcHeaders.putAll(hcHeaders);
        }
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
