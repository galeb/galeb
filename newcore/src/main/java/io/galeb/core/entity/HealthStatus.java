package io.galeb.core.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class HealthStatus extends AbstractEntity {

    @SuppressWarnings("unused")
    public enum Status {
        HEALTHY,
        FAIL,
        UNKNOWN
    }

    @ManyToOne
    private Target target;

    private Status status = Status.UNKNOWN;

    private String statusDetailed;

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getStatusDetailed() {
        return statusDetailed;
    }

    public void setStatusDetailed(String statusDetailed) {
        this.statusDetailed = statusDetailed;
    }
}
