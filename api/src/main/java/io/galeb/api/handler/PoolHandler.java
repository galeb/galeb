package io.galeb.api.handler;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Pool;
import org.springframework.stereotype.Component;

@Component
public class PoolHandler extends AbstractHandler<Pool> {

    @Override
    public Class<? extends AbstractEntity> entityClass() {
        return Pool.class;
    }
}
