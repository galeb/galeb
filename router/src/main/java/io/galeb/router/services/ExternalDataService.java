package io.galeb.router.services;

import io.galeb.router.cluster.EtcdClient;
import io.galeb.router.cluster.ExternalData;
import io.galeb.router.SystemEnvs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.boot.etcd.EtcdNode;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ExternalDataService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String ROOT_KEY         = "/";
    public static final String PREFIX_KEY       = ROOT_KEY + SystemEnvs.CLUSTER_ID.getValue();
    public static final String VIRTUALHOSTS_KEY = PREFIX_KEY + "/virtualhosts";
    public static final String POOLS_KEY        = PREFIX_KEY + "/pools";

    private final EtcdClient client;

    @Autowired
    public ExternalDataService(final EtcdClient etcdClient) {
        this.client = etcdClient;
    }

    public EtcdClient client() {
        return client;
    }

    public List<ExternalData> listFrom(String key) {
        return listFrom(key, false);
    }

    public List<ExternalData> listFrom(String key, boolean recursive) {
        return listFrom(node(key, recursive));
    }

    public List<ExternalData> listFrom(ExternalData node) {
        return node.getNodes();
    }

    public ExternalData node(String key) {
        return node(key, false);
    }

    public ExternalData node(String key, boolean recursive) {
        return node(key, recursive, ExternalData.Generic.NULL);
    }

    public ExternalData node(String key, ExternalData.Generic def) {
        return node(key, false, def);
    }

    public synchronized ExternalData node(String key, boolean recursive, ExternalData.Generic def) {
        try {
            final EtcdNode node = client.get(key, recursive).getNode();
            final ExternalData data = node != null ? new ExternalData(node) : def.instance();
            logger.debug("GET " + key + ": " +  "ExternalData(value=" + data.getValue() + ", dir=" + data.isDir() + ")");
            return data;
        } catch (Exception e) {
            logger.warn("GET " + key + " FAIL: " + e.getMessage());
            return def.instance();
        }
    }

    public synchronized boolean exist(String key) {
        try {
            final EtcdNode node = client.get(key, false).getNode();
            return node != null && (node.getValue() != null || node.isDir());
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

}
