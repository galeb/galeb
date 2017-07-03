/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.galeb.core.entity.annotations.JsonCustomProperties;
import io.galeb.core.entity.security.SpringSecurityAuditorAware;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@MappedSuperclass
@JsonCustomProperties
public abstract class AbstractEntity<T extends AbstractEntity<?>> implements Serializable {

    private static final long serialVersionUID = 4521414292400791447L;

    private static final Log LOGGER = LogFactory.getLog(AbstractEntity.class);

    public enum EntityStatus {
        PENDING,
        OK,
        ERROR,
        UNKNOWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    @Column(name = "_version")
    @JsonProperty("_version")
    private Long version;

    @CreatedBy
    @Column(name = "_created_by", nullable = false, updatable = false)
    @JsonProperty("_created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "_created_at", nullable = false, updatable = false)
    @JsonProperty("_created_at")
    private Date createdAt;

    @LastModifiedDate
    @Column(name = "_lastmodified_at", nullable = false)
    @JsonProperty("_lastmodified_at")
    private Date lastModifiedAt;

    @LastModifiedBy
    @Column(name = "_lastmodified_by", nullable = false)
    @JsonProperty("_lastmodified_by")
    private String lastModifiedBy;

    @Column(name = "name", nullable = false)
    @JsonProperty(required = true)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private final Map<String, String> properties = new HashMap<>();

    @Column(name = "_status")
    @JsonProperty("_status")
    protected EntityStatus status;

    @JsonIgnore
    @Transient
    private boolean saveOnly = false;

    private String description;

    private Integer hash = 0;

    @PrePersist
    private void onCreate() {
        createdAt = new Date();
        createdBy = getCurrentAuditor();
        lastModifiedAt = createdAt;
        lastModifiedBy = createdBy;
        saveOnly = false;
    }

    @PreUpdate
    private void onUpdate() {
        lastModifiedAt = new Date();
        lastModifiedBy = getCurrentAuditor();
    }

    private String getCurrentAuditor() {
        final SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();
        return auditorAware.getCurrentAuditor();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        updateHash();
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Date getLastModifiedAt() {
        return lastModifiedAt != null ? new Date(lastModifiedAt.getTime()) : null;
    }

    public Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
        Assert.hasText(name, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
        updateHash();
        this.name = name;
        return (T) this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @SuppressWarnings("unchecked")
    public T setProperties(Map<String, String> properties) {
        if (properties != null) {
            updateHash();
            this.properties.clear();
            this.properties.putAll(properties);
        }
        return (T) this;
    }

    public boolean isSaveOnly() {
        return saveOnly;
    }

    @SuppressWarnings("unchecked")
    public T setSaveOnly(boolean saveOnly) {
        this.saveOnly = saveOnly;
        return (T) this;
    }

    @Override
    public int hashCode() {
        if (name != null) {
            return name.hashCode();
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        T other = (T) obj;
        return other.getName() != null && other.getName().equals(getName());
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unchecked")
    public T setDescription(String description) {
        this.description = description;
        return (T) this;
    }

    public int getHash() {
        return hash;
    }

    @SuppressWarnings("unchecked")
    public T setHash(Integer hash) {
        if (hash != null) {
            this.hash = hash;
        } else {
            if (this.hash == null) {
                this.hash = 0;
            }
        }
        return (T) this;
    }

    public EntityStatus getStatus() {
        return status;
    }


    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    @JsonIgnore
    public void updateHash() {
        if (hash != null && hash < Integer.MAX_VALUE) {
            hash++;
        } else {
            hash = 0;
        }
    }

}
