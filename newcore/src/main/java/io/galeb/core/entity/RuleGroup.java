package io.galeb.core.entity;

import java.util.Set;

public class RuleGroup {

    private Set<VirtualHost> virtualHosts;
    private Set<Rule> rules;
    private Project project;

    public Set<VirtualHost> getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(Set<VirtualHost> virtualHosts) {
        this.virtualHosts = virtualHosts;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

}
