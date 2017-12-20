package io.galeb.api.services;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.WithStatus;
import io.galeb.core.entity.WithStatus.Status;
import org.springframework.stereotype.Service;

@Service
public class StatusService {

    public Status status(WithStatus entity, Long id) {
        return ((AbstractEntity)entity).isQuarantine() ? Status.DELETED : Status.OK;
    }
}
