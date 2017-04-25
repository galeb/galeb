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

package io.galeb.router.kv;

import java.util.List;

public interface ExternalData {
    
    @SuppressWarnings("unused")
    enum Generic {
        NULL(new ExternalData() {}),
        EMPTY(new ExternalData() { public String getValue() { return ""; }}),
        UNDEF(new ExternalData() { public String getValue() { return "UNDEF"; }}),
        ZERO(new ExternalData() { public String getValue() { return "0"; }});

        private final ExternalData node;
        Generic(final ExternalData node) {
            this.node = node;
        }

        public ExternalData instance() {
            return node;
        }
    }

    default String getKey() { return null; }

    default void setKey(String key) {}

    default String getValue() { return null; }

    default void setValue(String value) {}

    default boolean isDir() { return false; }

    default void setDir(boolean dir) {}

    default List<ExternalData> getNodes() { return null; }

    default void setNodes(List<ExternalData> nodes) {}

}
