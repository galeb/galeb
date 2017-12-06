package io.galeb.core.entity;

import java.io.Serializable;
import java.util.Date;

public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;

    private Long version;

    private String createdBy;

    private Date createdAt;

    private String lastModifieBy;

    private Date lastModifiedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModifieBy() {
        return lastModifieBy;
    }

    public void setLastModifieBy(String lastModifieBy) {
        this.lastModifieBy = lastModifieBy;
    }

    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

}
