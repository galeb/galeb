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

import io.galeb.router.client.hostselectors.HostSelector;
import io.galeb.router.client.hostselectors.HostSelectorLookup;
import io.galeb.router.client.hostselectors.RoundRobinHostSelector;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class RoundRobinHostSelectorTest extends AbstractHostSelectorTest {

    private final HostSelector roundRobinHostSelector = HostSelectorLookup.getHostSelector(getName(RoundRobinHostSelector.class));

    @Test
    public void testSelectHost() throws Exception {
        assertThat(roundRobinHostSelector, instanceOf(RoundRobinHostSelector.class));
        int loopFactor = 100;
        for (int retry = 1; retry <= NUM_RETRIES; retry++) {
            int loopLimit = (int) (NUM_HOSTS * Math.random() * loopFactor);
            IntStream.range(0, loopLimit).forEach(x -> {
                long result = roundRobinHostSelector.selectHost(hosts, commonExchange);
                assertThat(result, equalTo((long) x % NUM_HOSTS));
            });
            ((RoundRobinHostSelector)roundRobinHostSelector).reset();
        }
    }
} 
