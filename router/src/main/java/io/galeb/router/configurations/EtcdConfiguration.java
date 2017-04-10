/**
 *
 */

package io.galeb.router.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.zalando.boot.etcd.EtcdClient;

@Configuration
@Profile({ "production" })
public class EtcdConfiguration {

    @Bean("etcdClient")
    public EtcdClient etcdClient() {
        return new EtcdClient("http://127.0.0.1:2379");
    }
}
