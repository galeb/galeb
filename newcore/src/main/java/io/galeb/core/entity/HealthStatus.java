package io.galeb.core.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class HealthStatus extends AbstractEntity {

    @ManyToOne
    private Target target;

    private String name;

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
