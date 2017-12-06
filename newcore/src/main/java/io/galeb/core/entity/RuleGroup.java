package io.galeb.core.entity;

import java.util.Map;
import java.util.Set;

public class RuleGroup implements WithStatus {

    private Set<VirtualHost> virtualHosts;
    private Map<Integer, Rule> rules;
    private Project project;
    private String name;


    public Set<VirtualHost> getVirtualHosts() {
        return virtualHosts;
    }

    public void setVirtualHosts(Set<VirtualHost> virtualHosts) {
        this.virtualHosts = virtualHosts;
    }

    public Map<Integer, Rule> getRules() {
        return rules;
    }

    public void setRules(Map<Integer, Rule> rules) {
        this.rules = rules;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
