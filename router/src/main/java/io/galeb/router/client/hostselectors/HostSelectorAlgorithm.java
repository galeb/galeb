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

package io.galeb.router.client.hostselectors;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class HostSelectorAlgorithm {
    public static final HostSelectorAlgorithm ROUNDROBIN       = new HostSelectorAlgorithm("RoundRobin",      RoundRobinHostSelector.class);
    public static final HostSelectorAlgorithm STRICT_LEASTCONN = new HostSelectorAlgorithm("StrictLeastConn", StrictLeastConnHostSelector.class);
    public static final HostSelectorAlgorithm LEASTCONN        = new HostSelectorAlgorithm("LeastConn",       LeastConnWithRRHostSelector.class);
    public static final HostSelectorAlgorithm HASH_SOURCEIP    = new HostSelectorAlgorithm("HashSourceIp",    HashSourceIpHostSelector.class);
    public static final HostSelectorAlgorithm HASH_URIPATH     = new HostSelectorAlgorithm("HashUriPath",     HashUriPathHostSelector.class);

    private final Class<? extends HostSelector> klazz;
    private static final Map<String, Class<? extends HostSelector>> hostSelectorMap = new HashMap<>();

    private HostSelectorAlgorithm(String key, final Class<? extends HostSelector> klazz) {
        this.klazz = klazz;
        hostSelectorMap.put(key, klazz);
    }

    public HostSelector getHostSelector() throws IllegalAccessException, InstantiationException {
        return klazz.newInstance();
    }

    public static HostSelector getHostSelector(String name) throws InstantiationException, IllegalAccessException {
        Class<? extends HostSelector> hostSelectorClass = hostSelectorMap.get(name);
        if (hostSelectorClass == null) return ROUNDROBIN.getHostSelector();
        return hostSelectorClass.newInstance();
    }
}
