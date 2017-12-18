package io.galeb.api.handler;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.RuleGroup;
import org.springframework.stereotype.Component;

@Component
public class RuleGroupHandler extends AbstractHandler<RuleGroup> {

    @Override
    public Class<? extends AbstractEntity> entityClass() {
        return RuleGroup.class;
    }
}
