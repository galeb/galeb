package io.galeb.api.handler;

import io.galeb.api.repository.RuleGroupRepository;
import io.galeb.core.entity.RuleGroup;
import io.galeb.core.entity.VirtualHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VirtualHostHandler extends AbstractHandler<VirtualHost> {

    @Autowired
    RuleGroupRepository ruleGroupRepository;

    public void beforeCreate(VirtualHost virtualHost) {
        if (virtualHost.getRulegroup() == null) {
            RuleGroup ruleGroup = new RuleGroup();
            ruleGroupRepository.save(ruleGroup);
            virtualHost.setRulegroup(ruleGroup);
        }
    }

    public void beforeDelete(VirtualHost virtualHost) {

    }
}
