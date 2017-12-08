package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_pool_name_project_id", columnNames = { "name", "project_id" }) })
public class Pool extends AbstractEntity implements WithStatus {

    @ManyToMany(mappedBy = "pools")
    private Set<Rule> rules= new HashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false, foreignKey = @ForeignKey(name="FK_pool_environment"))
    private Environment environment;

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name = "target_id", foreignKey = @ForeignKey(name="FK_target_id")),
            inverseJoinColumns=@JoinColumn(name = "pool_id", nullable = false, foreignKey = @ForeignKey(name="FK_pool_id")))
    private Set<Target> targets = new HashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name="FK_pool_environment"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "balancepolicy_id", nullable = false, foreignKey = @ForeignKey(name="FK_pool_balancepolicy"))
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

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
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
