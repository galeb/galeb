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

package io.galeb.router.discovery.etcd;

import java.util.Objects;

public class EtcdResponse {

    private String action = null;
    private EtcdNode node = null;
    private EtcdNode prevNode = null;

    public EtcdResponse() {
    }

    public EtcdResponse(String action, EtcdNode node, EtcdNode prevNode) {
        this.action = action;
        this.node = node;
        this.prevNode = prevNode;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public EtcdNode getNode() {
        return node;
    }

    public void setNode(EtcdNode node) {
        this.node = node;
    }

    public EtcdNode getPrevNode() {
        return prevNode;
    }

    public void setPrevNode(EtcdNode prevNode) {
        this.prevNode = prevNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtcdResponse that = (EtcdResponse) o;
        return Objects.equals(action, that.action) &&
                Objects.equals(node, that.node) &&
                Objects.equals(prevNode, that.prevNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, node, prevNode);
    }
}
