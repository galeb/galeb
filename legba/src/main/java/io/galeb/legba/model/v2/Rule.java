package io.galeb.legba.model.v2;

import java.util.Set;

public class Rule {

    private Set<Pool> pools;
    private String matching;
    private boolean global;

    public Set<Pool> getPools() {
        return pools;
    }

    public void setPools(Set<Pool> pools) {
        this.pools = pools;
    }

    public String getMatching() {
        return matching;
    }

    public void setMatching(String matching) {
        this.matching = matching;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

}
