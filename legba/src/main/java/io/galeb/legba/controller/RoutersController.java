package io.galeb.legba.controller;

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
    public ResponseEntity<?> headRouterMap(@RequestHeader(value = "X-Galeb-LocalIP") String routerLocalIP,
                                           @RequestHeader(value = "X-Galeb-GroupID") String routerGroupId,
                                           @RequestHeader(value = "X-Galeb-Environment") String envName,
                                           @RequestHeader(value = "If-None-Match") String version) throws Exception {
        Long envId = getEnvironmentId(envName);
        routersService.put(routerGroupId, routerLocalIP, version, envId.toString());
        return ResponseEntity.ok().build();
    }

}
