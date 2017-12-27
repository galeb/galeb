package io.galeb.api.handler;

import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class PoolHandler extends AbstractHandler<Pool> {

    @Override
    protected Set<Environment> getAllEnvironments(Pool entity) {
        return Collections.singleton(entity.getEnvironment());
    }

}
