/**
 *
 */

package io.galeb.router.configurations;

import io.galeb.router.SystemEnvs;
import io.galeb.router.cluster.EtcdClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.zalando.boot.etcd.EtcdException;

import java.util.concurrent.ExecutionException;

@Configuration
@Profile({ "production" })
public class EtcdConfiguration {

    private final Log logger = LogFactory.getLog(this.getClass());

    private static final String ETCD_SERVER = SystemEnvs.ETCD_SERVER.getValue();

    @Bean
    public EtcdClient etcdClient() throws EtcdException, ExecutionException, InterruptedException {
        final EtcdClient etcdClient = new EtcdClient(ETCD_SERVER);
        logger.info("Using " + etcdClient);
        return etcdClient;
    }

}
