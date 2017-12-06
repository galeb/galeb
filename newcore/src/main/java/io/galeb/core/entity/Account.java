package io.galeb.core.entity;

import java.util.Set;

public class Account extends AbstractEntity  {

    private Set<Team> teams;

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
    }
}
