package io.galeb.router.cluster;

import org.zalando.boot.etcd.EtcdNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EtcdExternalData implements ExternalData {

    private final EtcdNode etcdNode = new EtcdNode();

    public EtcdExternalData() {
        // etcdNode default
    }

    public EtcdExternalData(EtcdNode etcdNode) {
        this.etcdNode.setKey(etcdNode.getKey());
        this.etcdNode.setValue(etcdNode.getValue());
        this.etcdNode.setDir(etcdNode.isDir());
        this.etcdNode.setNodes(etcdNode.getNodes());
    }

    @Override
    public String getKey() {
        return etcdNode.getKey();
    }

    @Override
    public void setKey(String key) {
        this.etcdNode.setKey(key);
    }

    @Override
    public String getValue() {
        return etcdNode.getValue();
    }

    @Override
    public void setValue(String value) {
        this.etcdNode.setValue(value);
    }

    @Override
    public boolean isDir() {
        return etcdNode.isDir();
    }

    @Override
    public void setDir(boolean dir) {
        this.etcdNode.setDir(dir);
    }

    @Override
    public List<ExternalData> getNodes() {
        final List<ExternalData> externalDatas = new ArrayList<>();
        Optional.ofNullable(etcdNode.getNodes()).orElse(Collections.emptyList()).forEach(n -> externalDatas.add(new EtcdExternalData(n)));
        return externalDatas;
    }

    @Override
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
