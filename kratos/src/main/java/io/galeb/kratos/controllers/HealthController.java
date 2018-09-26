package io.galeb.kratos.controllers;

import io.galeb.kratos.services.HealthService;
import io.galeb.kratos.services.HealthSchema;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/healths", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthController {

    @Autowired
    HealthService healthService;

    @GetMapping
    public ResponseEntity<Set<HealthSchema.Env>> get() {
        return ResponseEntity.ok(healthService.get());
    }

    @GetMapping(value = "/{envid:.+}")
    public ResponseEntity<Set<HealthSchema.Env>> get(@PathVariable(required = false) String envid) {
        return ResponseEntity.ok(healthService.get(envid));
    }

}
