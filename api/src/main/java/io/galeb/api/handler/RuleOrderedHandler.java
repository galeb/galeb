package io.galeb.api.handler;

import io.galeb.api.repository.EnvironmentRepository;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.RuleOrdered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RuleOrderedHandler extends AbstractHandler<RuleOrdered> {

    @Autowired
    EnvironmentRepository environmentRepository;

    @Override
    protected Set<Environment> getAllEnvironments(RuleOrdered entity) {
        return environmentRepository.findAllByRuleOrderedId(entity.getId());
    }

}
