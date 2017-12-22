package io.galeb.api.services;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.WithStatus.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StatusService {

    @Autowired
    ChangesService changesService;

    public Map<Long, Status> status(AbstractEntity entity) {
        Set<Long> allEnvironmentsWithChanges = changesService.listEnvironmentIds(entity);
        Set<Long> allEnvironmentIdsEntity = entity.getAllEnvironments().stream().map(Environment::getId).collect(Collectors.toSet());
        allEnvironmentIdsEntity.removeAll(allEnvironmentsWithChanges);

        Map<Long, Status> mapStatus = new HashMap<>();
        allEnvironmentIdsEntity.stream().forEach(e -> mapStatus.put(e, Status.OK));
        Status status = entity.isQuarantine() ? Status.DELETED : Status.PENDING;
        allEnvironmentsWithChanges.stream().forEach(e -> mapStatus.put(e, status));

        return mapStatus;
    }

}
