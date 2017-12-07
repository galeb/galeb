package io.galeb.core.entity;

import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Role extends AbstractEntity  {

    @Column(nullable = false)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.hasText(name, "name is not valid");
        this.name = name;
    }
}
