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

import static org.springframework.http.HttpStatus.OK;

import io.galeb.core.enums.SystemEnv;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.core.services.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"virtualhostscached", "{apiVersion:.+}/virtualhostscached"}, produces = MediaType.APPLICATION_JSON_VALUE)
public class VirtualHostCachedController extends AbstractController {

    private final VersionService versionService;

    @Autowired
    public VirtualHostCachedController(VersionService versionService) {
        this.versionService = versionService;
    }

    @RequestMapping(value="/{envName:.+}", method = RequestMethod.GET)
    public synchronized ResponseEntity showall(@PathVariable(required = false) String apiVersion,
                                               @PathVariable String envName,
                                               @RequestHeader(value = "If-None-Match", required = false) String routerVersion,
                                               @RequestHeader(value = "X-Galeb-GroupID", required = false) String routerGroupId,
                                               @RequestHeader(value = "X-Galeb-CacheHash", required = false) String routerCacheHash,
                                               @RequestHeader(value = "X-Galeb-ZoneID", required = false) String zoneId) throws Exception {

        final JsonEventToLogger event = new JsonEventToLogger(this.getClass());
        event.put("apiVersion", apiVersion);

        Assert.notNull(envName, "Environment name is null");
        Assert.notNull(routerGroupId, "GroupID undefined");
        Assert.notNull(routerVersion, "version undefined");

        Long envId = getEnvironmentId(envName);
        String actualVersion = versionService.getActualVersion(envId.toString());

        event.put("message", "Processing VirtualHostCached");
        event.put("actualVersion", actualVersion);
        event.put("routerVersion", routerVersion);
        event.put("routerCacheHash", routerCacheHash);
        event.put("environmentId", String.valueOf(envId));
        event.put("environmentName", envName);
        event.put("groupId", routerGroupId);
        event.put("zoneId", zoneId);

        String actualHash;
        try {
            actualHash = versionService.getActualHash(envId.toString(), zoneId, actualVersion);
        } catch (IllegalStateException e) {
            event.sendError(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (routerVersion.equals(actualVersion) && actualHash != null && actualHash.equals(routerCacheHash)) {
            event.put("status", HttpStatus.NOT_MODIFIED.toString());
            event.sendInfo();
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        String cache = versionService.getCache(envId.toString(), zoneId, actualVersion);
        if (cache == null || "".equals(cache)) {
            event.put("message", "Cache NOT FOUND");
            event.put("status", HttpStatus.NOT_FOUND.toString());
            event.sendInfo();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        event.put("status", HttpStatus.OK.toString());
        event.sendInfo();

        return new ResponseEntity<>(cache, OK);
    }
}
