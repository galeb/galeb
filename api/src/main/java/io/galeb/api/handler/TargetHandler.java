package io.galeb.api.handler;

import io.galeb.core.exceptions.BadRequestException;
import io.galeb.core.entity.Target;
import org.springframework.stereotype.Component;

@Component
public class TargetHandler extends AbstractHandler<Target> {

    @Override
    protected void onBeforeCreate(Target entity) {
        super.onBeforeCreate(entity);
        if (entity.getPools() == null || entity.getPools().isEmpty()) {
            throw new BadRequestException("Pool(s) undefined");
        }
    }
}
