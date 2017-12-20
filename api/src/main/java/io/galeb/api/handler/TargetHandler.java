package io.galeb.api.handler;

import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.core.exceptions.BadRequestException;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TargetHandler extends AbstractHandler<Target> {

    @Autowired
    EnvironmentRepository environmentRepository;

    @Override
    protected void onBeforeCreate(Target entity) {
        super.onBeforeCreate(entity);
        if (entity.getPools() == null || entity.getPools().isEmpty()) {
            throw new BadRequestException("Pool(s) undefined");
        }
    }

    @Override
    protected Set<Environment> getAllEnvironments(Target entity) {
        return environmentRepository.findAllByTargetId(entity.getId());
    }

}
