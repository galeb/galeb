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

package io.galeb.health.util;

import io.galeb.core.configuration.SystemEnvs;
import io.galeb.core.entity.Target;
import io.galeb.core.rest.ManagerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class TargetStamper {

    public enum HcState {
        FAIL,
        UNKNOWN,
        OK
    }

    public static final String PROP_HEALTHCHECK_RETURN = "hcBody";
    public static final String PROP_HEALTHCHECK_PATH   = "hcPath";
    public static final String PROP_HEALTHCHECK_HOST   = "hcHost";
    public static final String PROP_HEALTHCHECK_CODE   = "hcStatusCode";
    public static final String PROP_HEALTHY            = "healthy";
    public static final String PROP_STATUS_DETAILED    = "status_detailed";

    private final String managerUrl = SystemEnvs.MANAGER_URL.getValue();

    private final ManagerClient managerClient;

    @Autowired
    public TargetStamper(final ManagerClient managerClient) {
        this.managerClient = managerClient;
    }

    public void patchTarget(Target target) {
        String targetUrl = managerUrl + "/target/" + target.getId();
        String healthy = target.getProperties().get(PROP_HEALTHY);
        String statusDetailed = target.getProperties().get(PROP_STATUS_DETAILED);
        healthy = healthy != null ? healthy : HcState.UNKNOWN.toString();
        statusDetailed = statusDetailed != null ? statusDetailed : HcState.UNKNOWN.toString();
        managerClient.renewToken();
        String body = "{\"properties\": { \"" + PROP_HEALTHY + "\":\"" + healthy + "\",\"" + PROP_STATUS_DETAILED + "\":\"" + statusDetailed + "\" }}";
        managerClient.patch(targetUrl, body);
    }

}
