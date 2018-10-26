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

package io.galeb.core.entity.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TargetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JsonNode target;
    private final JsonNode pool;
    private final String correlation;

    public TargetDTO(Target target) {
        this.target = MAPPER.valueToTree(target);
        this.pool = MAPPER.valueToTree(target.getPool());
        this.correlation = UUID.randomUUID().toString();
    }

    public Target getTarget() {
        try {
            return MAPPER.treeToValue(target, Target.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public Pool getPool() {
        try {
            return MAPPER.treeToValue(pool, Pool.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TargetDTO that = (TargetDTO) o;
        return Objects.equals(getTarget(), that.getTarget()) &&
            Objects.equals(getPool(), that.getPool());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTarget(), getPool());
    }

    public String getCorrelation() {
        return this.correlation;
    }
}
