package io.galeb.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@RestController
public class HealthcheckController {

    @GetMapping(value = "/healthcheck")
    @ResponseBody
    public String healthcheck() {
        return "WORKING";
    }
}
