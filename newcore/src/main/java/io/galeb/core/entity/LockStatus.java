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

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class LockStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Expose
    private String name;
    @Expose
    private Date lastModifiedAt;
    @Expose
    private boolean hasLock;
    @Expose
    private Map<String, Integer> counterDownLatch;

    public String getName() {
        return name;
    }

    public Date getLastModifiedAt() {
        return new Date(lastModifiedAt.getTime());
    }

    public boolean isHasLock() {
        return hasLock;
    }

    public LockStatus() {
    }

}