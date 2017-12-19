package io.galeb.api.handler;

import io.galeb.api.repository.RuleGroupRepository;
import io.galeb.core.entity.RuleGroup;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VirtualHostHandler extends AbstractHandler<VirtualHost> {

    @Autowired
    RuleGroupRepository ruleGroupRepository;

    @Override
    protected void onBeforeCreate(VirtualHost virtualHost) {
        super.onBeforeCreate(virtualHost);
        if (virtualHost.getEnvironments() == null || virtualHost.getEnvironments().isEmpty()) {
            throw new BadRequestException("Environment(s) undefined");
        }
        if (virtualHost.getRulegroup() == null) {
            RuleGroup ruleGroup = new RuleGroup();
            ruleGroupRepository.save(ruleGroup);
            virtualHost.setRulegroup(ruleGroup);
        }
    }

}
