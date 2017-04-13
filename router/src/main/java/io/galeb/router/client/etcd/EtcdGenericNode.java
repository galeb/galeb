package io.galeb.router.client.etcd;

import org.zalando.boot.etcd.EtcdNode;

@SuppressWarnings("unused")
public enum EtcdGenericNode {
    NULL(new EtcdNode()),
    EMPTY(new EtcdNode() { public String getValue() { return ""; }}),
    UNDEF(new EtcdNode() { public String getValue() { return "UNDEF"; }}),
    ZERO(new EtcdNode() { public String getValue() { return "0"; }});

    private final EtcdNode node;
    EtcdGenericNode(final EtcdNode node) {
        this.node = node;
    }

    public EtcdNode get() {
        return node;
    }
}
