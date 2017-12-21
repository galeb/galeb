package io.galeb.api.services;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.WithStatus.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StatusService {

    @Autowired
    ChangesService changesService;

    public Status status(AbstractEntity entity) {
        Status status = Status.OK;
        if (changesService.has(entity)) {
            status = entity.isQuarantine() ? Status.DELETED : Status.PENDING;
        }
        return status;
    }

}
