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
import io.galeb.core.entity.Rule;
import io.galeb.core.entity.RuleOrdered;
import io.galeb.core.entity.Target;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.entity.VirtualhostGroup;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public class EntitiesRegistrable extends ArrayList<String> {

    private EntitiesRegistrable entitiesRegistrable = new EntitiesRegistrable();
    {
        add(Target.class.getSimpleName());
        add(Pool.class.getSimpleName());
        add(VirtualhostGroup.class.getSimpleName());
        add(VirtualHost.class.getSimpleName());
        add(RuleOrdered.class.getSimpleName());
        add(Rule.class.getSimpleName());
    }

    private EntitiesRegistrable(int initialCapacity) {
        super(initialCapacity);
    }

    private EntitiesRegistrable() {
    }

    private EntitiesRegistrable(Collection<? extends String> c) {
        super(c);
    }

    public static boolean contains(String entityClassName) {
        return entityClassName.contains(entityClassName);
    }
}
