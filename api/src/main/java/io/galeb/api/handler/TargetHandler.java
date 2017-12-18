package io.galeb.api.handler;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Target;
import org.springframework.stereotype.Component;

@Component
public class TargetHandler extends AbstractHandler<Target> {

    @Override
    public Class<? extends AbstractEntity> entityClass() {
        return Target.class;
    }
}
