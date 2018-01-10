package io.galeb.api.handler;

import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.api.repository.TargetRepository;
import io.galeb.core.exceptions.BadRequestException;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TargetHandler extends AbstractHandler<Target> {

    @Autowired
    EnvironmentRepository environmentRepository;

    @Autowired
    TargetRepository targetRepository;

    @Override
    protected void onBeforeCreate(Target entity) {
        super.onBeforeCreate(entity);
        if (entity.getPools() == null || entity.getPools().isEmpty()) {
            throw new BadRequestException("Pool(s) undefined");
        }
        if (targetRepository.findByNameAndPoolsIn(entity.getName(), entity.getPools(), new PageRequest(0, 9999)).hasContent()) {
            throw new BadRequestException("Target duplicated");
        }
    }

    @Override
    protected Set<Environment> getAllEnvironments(Target entity) {
        return environmentRepository.findAllByTargetId(entity.getId());
    }

}
