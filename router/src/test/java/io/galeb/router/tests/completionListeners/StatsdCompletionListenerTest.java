/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
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

package io.galeb.router.tests.completionListeners;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.AbstractMap;
import java.util.Map;

import org.junit.Test;

import io.galeb.router.handlers.completionListeners.StatsdCompletionListener;
import io.galeb.router.services.StatsdClientService;

public class StatsdCompletionListenerTest {

    private StatsdClientService statsdClientService = mock(StatsdClientService.class);
    private final StatsdCompletionListener statsdCompletionListener = new StatsdCompletionListener(statsdClientService);

    @Test
    public void cleanUpKeyTest() {
        Map<String, String> results = Map.ofEntries(
                new AbstractMap.SimpleEntry<String, String>("http://127.0.0.1", "127_0_0_1"),
                new AbstractMap.SimpleEntry<String, String>("a.b.c.d", "a_b_c_d"),
                new AbstractMap.SimpleEntry<String, String>("a b c d", "a_b_c_d"),
                new AbstractMap.SimpleEntry<String, String>("a:b:c:d", "a_b_c_d"),
                new AbstractMap.SimpleEntry<String, String>("a:b c.d_e", "a_b_c_d_e"));

        for (Map.Entry<String, String> entry : results.entrySet()) {
            assertTrue(statsdCompletionListener.cleanUpKey(entry.getKey()).equals(entry.getValue()));
        }
    }
}
