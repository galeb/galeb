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



import javax.persistence.*;
import java.util.Objects;

@NamedQueries({
        @NamedQuery(
                name = "HealthStatusDefault",
                query = "SELECT DISTINCT entity From HealthStatus entity INNER JOIN entity.target.pool pool INNER JOIN pool.project p INNER JOIN p.teams t INNER JOIN t.accounts a " +
                "WHERE a.username = :username")
})

@Entity
@Table(name = "health_status", uniqueConstraints = { @UniqueConstraint(name = "UK_health_status_source_target_id", columnNames = { "source", "target_id" }) })
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthStatus that = (HealthStatus) o;
        return Objects.equals(getTarget(), that.getTarget()) &&
                Objects.equals(getSource(), that.getSource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTarget(), getSource());
    }
}
