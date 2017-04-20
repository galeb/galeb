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

package io.galeb.router.cluster;

import org.zalando.boot.etcd.EtcdNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExternalData {

    @SuppressWarnings("unused")
    public enum Generic {
        NULL(new ExternalData()),
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

    private final EtcdNode etcdNode = new EtcdNode();

    public ExternalData() {
        // etcdNode default
    }

    public ExternalData(EtcdNode etcdNode) {
        this.etcdNode.setKey(etcdNode.getKey());
        this.etcdNode.setValue(etcdNode.getValue());
        this.etcdNode.setDir(etcdNode.isDir());
        this.etcdNode.setNodes(etcdNode.getNodes());
    }

    public String getKey() {
        return etcdNode.getKey();
    }

    public void setKey(String key) {
        this.etcdNode.setKey(key);
    }

    public String getValue() {
        return etcdNode.getValue();
    }

    public void setValue(String value) {
        this.etcdNode.setValue(value);
    }

    public boolean isDir() {
        return etcdNode.isDir();
    }

    public void setDir(boolean dir) {
        this.etcdNode.setDir(dir);
    }

    public List<ExternalData> getNodes() {
        final List<ExternalData> externalDatas = new ArrayList<>();
        Optional.ofNullable(etcdNode.getNodes()).orElse(Collections.emptyList()).forEach(n -> externalDatas.add(new ExternalData(n)));
        return externalDatas;
    }

    public void setNodes(List<ExternalData> nodes) {
        etcdNode.setNodes(convertListExternalDataToListEtcNode(nodes));
    }

    private List<EtcdNode> convertListExternalDataToListEtcNode(final List<ExternalData> nodes) {
        final List<EtcdNode> listOfEtcNodes = this.etcdNode.getNodes();
        nodes.forEach(n -> listOfEtcNodes.add(new EtcdNode(n.getKey(), n.getValue(), null, n.isDir(), 0, 0, null,
                n.getNodes().isEmpty() ? null : convertListExternalDataToListEtcNode(n.getNodes()))));
        return listOfEtcNodes;
    }

}
