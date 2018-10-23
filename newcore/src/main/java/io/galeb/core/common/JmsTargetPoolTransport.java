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

package io.galeb.core.common;

import io.galeb.core.entity.Pool;
import io.galeb.core.entity.Target;
import java.io.Serializable;
import java.util.Objects;

public class JmsTargetPoolTransport implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Target target;
    private final Pool pool;

    public JmsTargetPoolTransport(Target target, Pool pool) {
        this.target = target;
        this.pool = pool;
    }

    public Target getTarget() {
        return target;
    }

    public Pool getPool() {
        return pool;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JmsTargetPoolTransport that = (JmsTargetPoolTransport) o;
        return Objects.equals(target, that.target) &&
            Objects.equals(pool, that.pool);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, pool);
    }
}
