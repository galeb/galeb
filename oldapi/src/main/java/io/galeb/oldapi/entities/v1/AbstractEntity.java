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

package io.galeb.oldapi.entities.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEntity<T extends AbstractEntity<?>> extends AbstractEntitySyncronizable implements Serializable {

    private static final long serialVersionUID = 4521414292400791447L;

    public enum EntityStatus {
        PENDING,
        OK,
        ERROR,
        UNKNOWN
    }

    private Long id;

    @JsonProperty("_version")
    private Long version;

    @JsonProperty("_created_by")
    private String createdBy;

    @JsonProperty("_created_at")
    private Date createdAt;

    @JsonProperty("_lastmodified_at")
    private Date lastModifiedAt;

    @JsonProperty("_lastmodified_by")
    private String lastModifiedBy;

    private String name;

    private final Map<String, String> properties = new HashMap<>();

    @JsonProperty("_status")
    protected EntityStatus status;

    @JsonIgnore
    private boolean saveOnly = false;

    private String description;

    private Integer hash = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        this.name = name;
        return (T) this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @SuppressWarnings("unchecked")
    public T setProperties(Map<String, String> properties) {
        if (properties != null) {
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

}
