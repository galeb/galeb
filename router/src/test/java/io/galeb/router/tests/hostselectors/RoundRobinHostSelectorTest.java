package io.galeb.router.tests.hostselectors;

import io.galeb.router.client.hostselectors.RoundRobinHostSelector;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class RoundRobinHostSelectorTest extends AbstractHostSelectorTest {

    private final RoundRobinHostSelector roundRobinHostSelector = new RoundRobinHostSelector();

    @Test
    public void testSelectHost() throws Exception {
        int loopFactor = 100;
        for (int retry = 1; retry <= NUM_RETRIES; retry++) {
            int loopLimit = (int) (NUM_HOSTS * Math.random() * loopFactor);
            IntStream.range(1, loopLimit).forEach(x -> {
                long result = roundRobinHostSelector.selectHost(hosts, commonExchange);
                assertThat(result, equalTo((long) x % NUM_HOSTS));
            });
            roundRobinHostSelector.reset();
        }
    }
} 
