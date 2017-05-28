/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.router.tests.hostselectors;

import io.galeb.router.client.ExtendedLoadBalancingProxyClient.Host;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

abstract class AbstractHashHostSelectorTest extends AbstractHostSelectorTest {

    void doRandomTest(double errorPercentMax, double limitOfNotHitsPercent, int numPopulation) {
        final HttpServerExchange exchange = new HttpServerExchange(null);
        final Host[] newHosts = numPopulation < hosts.length ? Arrays.copyOf(hosts, numPopulation) : hosts;
        final Map<Integer, String> remains = IntStream.rangeClosed(0, newHosts.length - 1).boxed().collect(Collectors.toMap(x -> x, x -> ""));

        for (int retry = 1; retry <= NUM_RETRIES; retry++) {
            final SummaryStatistics statisticsOfResults = new SummaryStatistics();

            final Map<Integer, Integer> mapOfResults = new HashMap<>();
            new Random().ints(numPopulation).map(Math::abs).forEach(x -> {
                changeExchange(exchange, x);
                int result = getResult(exchange, newHosts);
                Integer lastCount = mapOfResults.get(result);
                remains.remove(result);
                mapOfResults.put(result, lastCount != null ? ++lastCount : 0);
            });
            mapOfResults.entrySet().stream().mapToDouble(Map.Entry::getValue).forEach(statisticsOfResults::addValue);
            double errorPercent = (statisticsOfResults.getStandardDeviation() / numPopulation) * 100;
            assertThat(errorPercent, lessThan(errorPercentMax));
        }
        final List<Integer> listOfNotHit = remains.entrySet().stream().map(Map.Entry::getKey).collect(toList());
        assertThat(listOfNotHit.size(), lessThanOrEqualTo((int) (newHosts.length * (limitOfNotHitsPercent / 100))));
    }

    abstract int getResult(HttpServerExchange exchange, Host[] newHosts);

    abstract void changeExchange(HttpServerExchange exchange, int x);
}
