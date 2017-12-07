package io.galeb.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class RoleGroup extends AbstractEntity  {

    @Column(nullable = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
