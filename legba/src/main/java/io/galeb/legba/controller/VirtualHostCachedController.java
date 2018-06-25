package io.galeb.legba.controller;

import com.google.gson.Gson;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.services.VersionService;
import io.galeb.legba.conversors.Converter;
import io.galeb.legba.conversors.ConverterBuilder;
import io.galeb.legba.services.CopyService;
import io.galeb.legba.services.RoutersService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = {"virtualhostscached", "{apiVersion:.+}/virtualhostscached"}, produces = MediaType.APPLICATION_JSON_VALUE)
public class VirtualHostCachedController extends AbstractController {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostCachedController.class);
    private final Gson gson = new Gson();

    @Autowired
    private VersionService versionService;

    @Autowired
    private CopyService copyService;

    @Autowired
    private RoutersService routersService;

    @RequestMapping(value="/{envName:.+}", method = RequestMethod.GET)
    public synchronized ResponseEntity showall(@PathVariable(required = false) String apiVersion,
                                               @PathVariable String envName,
                                               @RequestHeader(value = "If-None-Match", required = false) String version,
                                               @RequestHeader(value = "X-Galeb-GroupID", required = false) String routerGroupId,
                                               @RequestHeader(value = "X-Galeb-ZoneID", required = false) String zoneId) throws Exception {
        Assert.notNull(envName, "Environment name is null");
        Assert.notNull(routerGroupId, "GroupID undefined");
        Assert.notNull(version, "version undefined");
        Long envId = getEnvironmentId(envName);
        String actualVersion = versionService.getActualVersion(envId.toString());

        Map<String, String> mapLog = new HashMap<>();
        mapLog.put("actualVersion", actualVersion);
        mapLog.put("environmentId", String.valueOf(envId));
        mapLog.put("environmentName", envName);
        mapLog.put("groupId", routerGroupId);
        mapLog.put("zoneId", zoneId);

        if (version.equals(actualVersion)) {
            mapLog.put("status", HttpStatus.NOT_MODIFIED.toString());
            LOGGER.info(gson.toJson(mapLog));

            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        String cache = versionService.getCache(envId.toString(), zoneId, actualVersion);
        if (cache == null) {
            Converter converter = ConverterBuilder.getConversor(apiVersion);
            String numRouters = String.valueOf(routersService.get(envId.toString(), routerGroupId));
            List<VirtualHost> list = copyService.getVirtualHosts(envId);
            cache = converter.convertToString(list, numRouters, actualVersion, zoneId, envId);
            versionService.setCache(cache, envId.toString(), zoneId, actualVersion);

            mapLog.put("apiVersion", apiVersion == null ? "default" : apiVersion);
            mapLog.put("numRouters", numRouters);
            mapLog.put("numVirtualHost", String.valueOf(list.size()));
        }
        if ("".equals(cache)) {
            mapLog.put("status", HttpStatus.NOT_FOUND.toString());
            LOGGER.info(gson.toJson(mapLog));

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        mapLog.put("status", HttpStatus.OK.toString());
        LOGGER.info(gson.toJson(mapLog));

        return new ResponseEntity<>(cache, OK);
    }
}
