package io.galeb.router.tests.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient;
import io.galeb.router.client.hostselectors.LeastConnHostSelector;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LeastConnHostSelectorTest extends AbstractHostSelectorTest {

    private final LeastConnHostSelector leastConnHostSelector = new LeastConnHostSelector();

    @Test
    public void testSelectHost() throws Exception {
        for (int retry = 1; retry <= NUM_RETRIES; retry++) {
            int hostsLength = new Random().nextInt(NUM_HOSTS);
            IntStream.range(0, hostsLength - 1).forEach(x -> {
                final ExtendedLoadBalancingProxyClient.Host[] newHosts = Arrays.copyOf(hosts, hostsLength);
                long result = leastConnHostSelector.selectHost(newHosts, commonExchange);
                assertThat(result, equalTo(0L));
            });
        }
    }
}
