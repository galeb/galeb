
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

package io.galeb.router.tests.suites;

import io.galeb.router.tests.client.ExtendedLoadBalancingProxyClientTest;
import io.galeb.router.tests.cucumber.CucumberTest;
import io.galeb.router.tests.handlers.PathGlobHandlerTest;
import io.galeb.router.tests.handlers.RequestIDHandlerTest;
import io.galeb.router.tests.hostselectors.GuavaConsistentHashTest;
import io.galeb.router.tests.hostselectors.HashSourceIpHostSelectorTest;
import io.galeb.router.tests.hostselectors.HashUriPathHostSelectorTest;
import io.galeb.router.tests.hostselectors.LeastConnHostSelectorTest;
import io.galeb.router.tests.hostselectors.LeastConnWithRRHostSelectorTest;
import io.galeb.router.tests.hostselectors.RoundRobinHostSelectorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LeastConnHostSelectorTest.class,
        LeastConnWithRRHostSelectorTest.class,
        RoundRobinHostSelectorTest.class,
        HashUriPathHostSelectorTest.class,
        HashSourceIpHostSelectorTest.class,
        ExtendedLoadBalancingProxyClientTest.class,
        GuavaConsistentHashTest.class,
        PathGlobHandlerTest.class,
        RequestIDHandlerTest.class,
        CucumberTest.class
})
public class ManagerTestSuite {

}
