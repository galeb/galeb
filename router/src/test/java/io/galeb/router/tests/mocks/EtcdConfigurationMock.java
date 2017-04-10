package io.galeb.router.tests.mocks;

import static org.mockito.Mockito.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.zalando.boot.etcd.EtcdClient;

@Configuration
@Profile({ "test" })
public class EtcdConfigurationMock {

    private static final Log LOGGER = LogFactory.getLog(EtcdConfigurationMock.class);

    private EtcdClient etcdClientMocked = mock(EtcdClient.class);

    @Bean("etcdClient")
    public EtcdClient etcdClient() {
        LOGGER.info("Using " + this.getClass().getSimpleName());
        return etcdClientMocked;
    }
}
