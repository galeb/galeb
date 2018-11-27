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

package io.galeb.legba.controller;

import static io.galeb.core.common.GalebHttpHeaders.*;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;

import com.google.gson.Gson;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.core.services.VersionService;
import io.galeb.legba.services.RoutersService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/routers", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoutersController extends AbstractController {

    private final Gson gson = new Gson();

    @Autowired
    RoutersService routersService;

    @Autowired
    VersionService versionService;

    @RequestMapping(method = RequestMethod.GET)
    public String routerMap() {
        return gson.toJson(routersService.get());
    }

    @RequestMapping(value = "/{envid:.+}", method = RequestMethod.GET)
    public String routerMap(@PathVariable(required = false) String envid) {
        return gson.toJson(routersService.get(envid));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> registerRouter(@RequestHeader(value = X_GALEB_LOCAL_IP) String routerLocalIP,
                                           @RequestHeader(value = X_GALEB_GROUP_ID) String routerGroupId,
                                           @RequestHeader(value = X_GALEB_ENVIRONMENT) String envName,
                                           @RequestHeader(value = IF_NONE_MATCH) String version,
                                           @RequestHeader(value = X_GALEB_ZONE_ID, required = false) String zoneId) throws Exception {
        final Long envId = getEnvironmentId(envName);
        String actualVersion = versionService.getActualVersion(envId.toString());

        final RouterMeta routerMeta = new RouterMeta();
        routerMeta.envId = envId.toString();
        routerMeta.groupId = routerGroupId;
        routerMeta.localIP = routerLocalIP;
        routerMeta.version = "EMPTY".equals(version) ? "0" : version;
        routerMeta.actualVersion = actualVersion;
        routerMeta.zoneId = zoneId;
        routerMeta.correlation = UUID.randomUUID().toString();

        JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("message", "Calling RoutersController.registerRouter");
        event.put("envId", routerMeta.envId);
        event.put("groupId", routerMeta.groupId);
        event.put("localIP", routerMeta.localIP);
        event.put("routerVersion", routerMeta.version);
        event.put("actualVersion", actualVersion);
        event.put("zoneId", routerMeta.zoneId);
        event.put("correlation", routerMeta.correlation);
        event.sendInfo();

        routersService.put(routerMeta);
        return ResponseEntity.ok().build();
    }

    public static class RouterMeta {
        public String groupId, localIP, version, actualVersion, envId, zoneId, correlation;
    }

}
