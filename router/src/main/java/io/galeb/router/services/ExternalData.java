package io.galeb.router.services;

import io.galeb.router.Application;
import io.galeb.router.client.etcd.EtcdClient;
import io.galeb.router.client.etcd.EtcdGenericNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.boot.etcd.EtcdNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ExternalData {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String ROOT_KEY         = "/";
    public static final String PREFIX_KEY       = ROOT_KEY + Application.PREFIX;
    public static final String VIRTUALHOSTS_KEY = PREFIX_KEY + "/virtualhosts";
    public static final String POOLS_KEY        = PREFIX_KEY + "/pools";

    private final EtcdClient client;

    @Autowired
    public ExternalData(@Value("#{etcdClient}") final EtcdClient etcdClient) {
        this.client = etcdClient;
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
        return node(key, recursive, EtcdGenericNode.NULL);
    }

    public EtcdNode node(String key, EtcdGenericNode def) {
        return node(key, false, def);
    }

    public synchronized EtcdNode node(String key, boolean recursive, EtcdGenericNode def) {
        try {
            final EtcdNode node = client.get(key, recursive).getNode();
            return node != null ? node : def.get();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
            return def.get();
        }
    }

    public synchronized boolean exist(String key) {
        final EtcdNode node = node(key);
        return node.getValue() != null || node.isDir();
    }

}
