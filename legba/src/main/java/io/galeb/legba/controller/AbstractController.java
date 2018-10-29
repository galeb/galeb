package io.galeb.legba.controller;

import io.galeb.legba.repository.EnvironmentRepository;
import org.h2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractController {

    @Autowired
    private EnvironmentRepository environmentRepository;

    protected Long getEnvironmentId(String envname) {
        if (StringUtils.isNumber(envname)) {
            return Long.parseLong(envname);
        } else {
            return environmentRepository.idFromName(envname);
        }
    }
}
