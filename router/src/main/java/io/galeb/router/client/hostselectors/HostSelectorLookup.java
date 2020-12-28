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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class HostSelectorLookup {
    private static final Map<String, Class<? extends HostSelector>> hostSelectorMap = new HashMap<>();

    public static final HostSelectorLookup ROUNDROBIN       = new HostSelectorLookup("RoundRobin",      RoundRobinHostSelector.class);
    public static final HostSelectorLookup STRICT_LEASTCONN = new HostSelectorLookup("StrictLeastConn", StrictLeastConnHostSelector.class);
    public static final HostSelectorLookup LEASTCONN        = new HostSelectorLookup("LeastConn",       LeastConnWithRRHostSelector.class);
    public static final HostSelectorLookup HASH_SOURCEIP    = new HostSelectorLookup("HashSourceIp",    HashSourceIpHostSelector.class);
    public static final HostSelectorLookup HASH_URIPATH     = new HostSelectorLookup("HashUriPath",     HashUriPathHostSelector.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(HostSelectorLookup.class);

    private final String name;
    private final Class<? extends HostSelector> klazz;

    private HostSelectorLookup(String name, final Class<? extends HostSelector> klazz) {
        this.name = name;
        this.klazz = klazz;
        hostSelectorMap.put(name, klazz);
    }

    public HostSelector getHostSelector() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return klazz.getDeclaredConstructor().newInstance();
    }

    @Override
    public String toString() {
        return name;
    }

    public static HostSelector getHostSelector(String name) {
        Class<? extends HostSelector> hostSelectorClass = hostSelectorMap.get(name);
        if (hostSelectorClass == null) {
            LOGGER.warn("HostSelector " + name + " not found. Using default.");
            return defaultHostSelector();
        }
        try {
            return hostSelectorClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
            return defaultHostSelector();
        }
    }

    private static HostSelector defaultHostSelector() {
        return new RoundRobinHostSelector();
    }
}
