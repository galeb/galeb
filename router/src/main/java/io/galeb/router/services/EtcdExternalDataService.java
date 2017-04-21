package io.galeb.router.services;

import io.galeb.router.cluster.EtcdClient;
import io.galeb.router.cluster.EtcdExternalData;
import io.galeb.router.cluster.ExternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.boot.etcd.EtcdNode;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class EtcdExternalDataService implements ExternalDataService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EtcdClient client;

    @Autowired
    public EtcdExternalDataService(final EtcdClient etcdClient) {
        this.client = etcdClient;
    }

    @Override
    public List<ExternalData> listFrom(String key) {
        return listFrom(key, false);
    }

    @Override
    public List<ExternalData> listFrom(String key, boolean recursive) {
        return listFrom(node(key, recursive));
    }

    @Override
    public List<ExternalData> listFrom(ExternalData node) {
        return node.getNodes();
    }

    @Override
    public ExternalData node(String key) {
        return node(key, false);
    }

    @Override
    public ExternalData node(String key, boolean recursive) {
        return node(key, recursive, ExternalData.Generic.NULL);
    }

    @Override
    public ExternalData node(String key, ExternalData.Generic def) {
        return node(key, false, def);
    }

    @Override
    public synchronized ExternalData node(String key, boolean recursive, ExternalData.Generic def) {
        try {
            final EtcdNode node = client.get(key, recursive).getNode();
            final ExternalData data = node != null ? new EtcdExternalData(node) : def.instance();
            logger.debug("GET " + key + ": " +  "ExternalData(value=" + data.getValue() + ", dir=" + data.isDir() + ")");
            return data;
        } catch (Exception e) {
            logger.warn("GET " + key + " FAIL: " + e.getMessage());
            return def.instance();
        }
    }

    @Override
    public synchronized boolean exist(String key) {
        try {
            final EtcdNode node = client.get(key, false).getNode();
            return node != null && (node.getValue() != null || node.isDir());
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

}
