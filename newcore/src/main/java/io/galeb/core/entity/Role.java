package io.galeb.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Role extends AbstractEntity  {

    @Column(name = "name", nullable = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
