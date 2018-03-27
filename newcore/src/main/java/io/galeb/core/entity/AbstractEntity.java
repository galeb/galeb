/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
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
import io.galeb.core.security.SpringSecurityAuditorAware;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
@JsonCustomProperties
public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Version
    @JsonProperty("_version")
    private Long version;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    @JsonProperty("_created_by")
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonProperty("_created_at")
    private Date createdAt;

    @LastModifiedBy
    @Column(nullable = false)
    @JsonProperty("_last_modified_by")

    private String lastModifiedBy;

    @LastModifiedDate
    @Column(nullable = false)
    @JsonProperty("_last_modified_at")
    private Date lastModifiedAt;

    @JsonIgnore
    private Boolean quarantine = false;

    @JsonIgnore
    @Transient
    private Set<Environment> allEnvironments = new HashSet<>();

    @PrePersist
    private void onCreate() {
        createdAt = new Date();
        createdBy = getCurrentAuditor();;
        lastModifiedAt = createdAt;
        lastModifiedBy = createdBy;
    }

    @PreUpdate
    private void onUpdate() {
        lastModifiedAt = new Date();
        lastModifiedBy = "";
    }

    private String description;

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

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Date lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isQuarantine() {
        return quarantine;
    }

    public void quarantine(Boolean quarantine) {
        this.quarantine = quarantine;
    }

    public Set<Environment> getAllEnvironments() {
        return allEnvironments;
    }

    public void setAllEnvironments(Set<Environment> allEnvironments) {
        this.allEnvironments = allEnvironments;
    }

    private String getCurrentAuditor() {
        final SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();
        return auditorAware.getCurrentAuditor();
    }

}
