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

import io.galeb.router.handlers.completionListeners.StatsdCompletionListener;
import io.galeb.router.services.StatsdClientService;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class StatsdCompletionListenerTest {

    private StatsdClientService statsdClientService = mock(StatsdClientService.class);
    private final StatsdCompletionListener statsdCompletionListener = new StatsdCompletionListener(statsdClientService);
    private final Map<String, String> results = new HashMap<String, String>(){{
        put("http://127.0.0.1","127_0_0_1");
        put("a.b.c.d", "a_b_c_d");
        put("a b c d", "a_b_c_d");
        put("a:b:c:d", "a_b_c_d");
        put("a:b c.d_e", "a_b_c_d_e");
    }};

    @Test
    public void cleanUpKeyTest() {
        for (Map.Entry<String, String> entry: results.entrySet()) {
            assertTrue(statsdCompletionListener.cleanUpKey(entry.getKey()).equals(entry.getValue()));
        }
    }
}
