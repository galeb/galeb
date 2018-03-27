package io.galeb.kratos.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    @GetMapping(value = "/info")
    public ResponseEntity<String> info() {
        String body = String.format("{\"name\":\"%s\", \"version\":\"%s\", \"build\":\"%s\", \"health\":\"WORKING\"}", buildProject, buildVersion, buildTimestamp);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("content-type", MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
