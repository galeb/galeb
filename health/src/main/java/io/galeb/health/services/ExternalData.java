package io.galeb.health.services;

import io.galeb.health.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.boot.etcd.EtcdClient;
import org.zalando.boot.etcd.EtcdNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ExternalData {

    public enum GenericNode {
        NULL(new EtcdNode()),
        EMPTY(new EtcdNode() { public String getValue() { return ""; }}),
        UNDEF(new EtcdNode() { public String getValue() { return "UNDEF"; }}),
        ZERO(new EtcdNode() { public String getValue() { return "0"; }});

        private final EtcdNode node;
        GenericNode(final EtcdNode node) {
            this.node = node;
        }

        public EtcdNode get() {
            return node;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String ROOT_KEY         = "/";
    public static final String PREFIX_KEY       = ROOT_KEY + Application.PREFIX;
    public static final String VIRTUALHOSTS_KEY = PREFIX_KEY + "/virtualhosts";
    public static final String POOLS_KEY        = PREFIX_KEY + "/pools";

    private final EtcdClient client;

    public ExternalData(@Value("#{etcdClient}") final EtcdClient template) {
        this.client = template;
    }

    public EtcdClient client() {
        return client;
    }

    public List<EtcdNode> listFrom(String key) {
        return listFrom(key, false);
    }

    public List<EtcdNode> listFrom(String key, boolean recursive) {
        return listFrom(node(key, recursive));
    }

    public List<EtcdNode> listFrom(EtcdNode node) {
        return Optional.ofNullable(node.getNodes()).orElse(Collections.emptyList());
    }

    public EtcdNode node(String key) {
        return node(key, false);
    }

    public EtcdNode node(String key, boolean recursive) {
        return node(key, recursive, GenericNode.NULL);
    }

    public EtcdNode node(String key, GenericNode def) {
        return node(key, false, def);
    }

    public EtcdNode node(String key, boolean recursive, GenericNode def) {
        try {
            return client.get(key, recursive).getNode();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return def.get();
        }
    }

    public boolean exist(String key) {
        final EtcdNode node = node(key);
        return node.getValue() != null || node.isDir();
    }

    public boolean endsWith(final EtcdNode node, String key) {
        return node.getKey().endsWith(key);
    }

}
