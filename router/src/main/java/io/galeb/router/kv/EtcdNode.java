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

import java.util.Date;
import java.util.List;

public class EtcdNode {

    private String key = null;
    private String value = null;
    private Long ttl = 0L;
    private boolean dir = false;
    private int createdIndex = 0;
    private int modifiedIndex = 0;
    private Date expiration = null;
    private List<EtcdNode> nodes = null;

    public EtcdNode() {
    }

    public EtcdNode(String key, String value, Long ttl, boolean dir, int createdIndex, int modifiedIndex, Date expiration, List<EtcdNode> nodes) {
        this.key = key;
        this.value = value;
        this.ttl = ttl;
        this.dir = dir;
        this.createdIndex = createdIndex;
        this.modifiedIndex = modifiedIndex;
        this.expiration = expiration;
        this.nodes = nodes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public boolean isDir() {
        return dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public int getCreatedIndex() {
        return createdIndex;
    }

    public void setCreatedIndex(int createdIndex) {
        this.createdIndex = createdIndex;
    }

    public int getModifiedIndex() {
        return modifiedIndex;
    }

    public void setModifiedIndex(int modifiedIndex) {
        this.modifiedIndex = modifiedIndex;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public List<EtcdNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<EtcdNode> nodes) {
        this.nodes = nodes;
    }
}
