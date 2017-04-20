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

@SuppressWarnings("unused")
public enum HostSelectorAlgorithm {
    ROUNDROBIN       (RoundRobinHostSelector.class),
    STRICT_LEASTCONN (LeastConnHostSelector.class),
    LEASTCONN        (LeastConnWithRRHostSelector.class),
    HASH_SOURCEIP    (HashSourceIpHostSelector.class),
    HASH_URIPATH     (HashUriPathHostSelector.class);

    private final Class klazz;
    HostSelectorAlgorithm(final Class klazz) {
        this.klazz = klazz;
    }

    public HostSelector getHostSelector() throws IllegalAccessException, InstantiationException {
        return (HostSelector) klazz.newInstance();
    }
}
