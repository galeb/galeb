package io.galeb.legba.controller;

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

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = "{apiVersion:.+}/virtualhostscached", produces = MediaType.APPLICATION_JSON_VALUE)
public class VirtualHostCachedController {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostCachedController.class);

    @Autowired
    private VersionService versionService;

    @Autowired
    private CopyService copyService;

    @Autowired
    private RoutersService routersService;

    @RequestMapping(value="/{envid:.+}", method = RequestMethod.GET)
    public synchronized ResponseEntity showall(@PathVariable String apiVersion,
                                               @PathVariable String envid,
                                               @RequestHeader(value = "If-None-Match", required = false) String version,
                                               @RequestHeader(value = "X-Galeb-GroupID", required = false) String routerGroupId,
                                               @RequestHeader(value = "X-Galeb-NetworkID", required = false) String networkId) throws Exception {
        Assert.notNull(envid, "Environment id is null");
        Assert.notNull(routerGroupId, "GroupID undefined");
        Assert.notNull(version, "version undefined");

        if (version.equals(versionService.getActualVersion(envid))) {
            LOGGER.warn("If-None-Match header matchs with internal etag, then ignoring request");
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        String cache = versionService.getCache(envid, version);
        if (cache == null) {
            Converter converter = ConverterBuilder.getConversor(apiVersion);
            String numRouters = routersService.get(envid, routerGroupId);
            List<VirtualHost> list = copyService.getVirtualHosts(envid);
            cache = converter.convertToString(list, numRouters, version, networkId);
            versionService.setCache(cache, envid, version);
        }
        if ("".equals(cache)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return new ResponseEntity<>(cache, OK);

    }

}
