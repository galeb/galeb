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
import io.galeb.legba.services.RoutersService;
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

    @RequestMapping(method = RequestMethod.GET)
    public String routerMap() {
        return gson.toJson(routersService.get());
    }

    @RequestMapping(value = "/{envid:.+}", method = RequestMethod.GET)
    public String routerMap(@PathVariable(required = false) String envid) {
        return gson.toJson(routersService.get(envid));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> headRouterMap(@RequestHeader(value = X_GALEB_LOCAL_IP) String routerLocalIP,
                                           @RequestHeader(value = X_GALEB_GROUP_ID) String routerGroupId,
                                           @RequestHeader(value = X_GALEB_ENVIRONMENT) String envName,
                                           @RequestHeader(value = IF_NONE_MATCH) String version,
                                           @RequestHeader(value = X_GALEB_ZONE_ID, required = false) String zoneId) throws Exception {
        Long envId = getEnvironmentId(envName);
        routersService.put(routerGroupId, routerLocalIP, version, envId.toString(), zoneId);
        return ResponseEntity.ok().build();
    }

}
