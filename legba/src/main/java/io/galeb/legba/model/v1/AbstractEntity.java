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

package io.galeb.legba.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEntity<T extends AbstractEntity<?>> {

    public enum EntityStatus {
        PENDING,
        OK,
        ERROR,
        UNKNOWN
    }

    private long id;

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

    private Integer hash = 0;

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
