package io.galeb.core.entity;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_healthstatus_name", columnNames = { "name" }) })
public class HealthStatus extends AbstractEntity {

    @SuppressWarnings("unused")
    public enum Status {
        HEALTHY,
        FAIL,
        UNKNOWN
    }

    @ManyToOne
    @JoinColumn(name = "target_id", nullable = false, foreignKey = @ForeignKey(name="FK_healthstatus_target"))
    private Target target;

    @Enumerated(EnumType.STRING)
    private Status status = Status.UNKNOWN;

    private String statusDetailed;

    private String source;

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
