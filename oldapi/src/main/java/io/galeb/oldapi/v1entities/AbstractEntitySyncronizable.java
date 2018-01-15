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

package io.galeb.oldapi.v1entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractEntitySyncronizable {

    @JsonIgnore
    public String getEnvName() { return "NULL"; }

    protected AbstractEntity.EntityStatus getDynamicStatus() {
        return AbstractEntity.EntityStatus.OK;
    }

    protected Farm getFakeFarm() {
        return new Farm().setName("fake").setAutoReload(false);
    }
}
