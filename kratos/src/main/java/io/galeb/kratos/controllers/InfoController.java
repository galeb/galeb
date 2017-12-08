package io.galeb.kratos.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    @Value("${build.project}")
    private String buildProject;

    @Value("${build.version}")
    private String buildVersion;

    @Value("${build.timestamp}")
    private String buildTimestamp;

    @GetMapping("/info")
    public String info() {
        return String.format("{\"name\":\"%s\", \"version\":\"%s\", \"build\":\"%s\"}", buildProject, buildVersion, buildTimestamp);
    }
}
