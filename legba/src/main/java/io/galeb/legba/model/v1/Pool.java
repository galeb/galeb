package io.galeb.legba.model.v1;

import java.util.Set;

public class Pool {

    private String name;
    private Set<Target> targets;
    private String discoveredMembersSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        this.targets = targets;
    }

    public String getDiscoveredMembersSize() {
        return discoveredMembersSize;
    }

    public void setDiscoveredMembersSize(String discoveredMembersSize) {
        this.discoveredMembersSize = discoveredMembersSize;
    }

}
