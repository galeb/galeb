package io.galeb.api.services;

import io.galeb.core.entity.WithStatus;
import org.springframework.stereotype.Service;

@Service
public class StatusService {

    public WithStatus.Status status(WithStatus withStatus, Long id) {
        return WithStatus.Status.OK;
    }
}
