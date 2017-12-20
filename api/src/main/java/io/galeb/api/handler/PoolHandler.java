package io.galeb.api.handler;

import com.google.common.collect.Sets;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.Pool;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PoolHandler extends AbstractHandler<Pool> {

    @Override
    protected Set<Environment> getAllEnvironments(Pool entity) {
        return Sets.newHashSet(entity.getEnvironment());
    }

}
