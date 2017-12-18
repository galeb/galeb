package io.galeb.api.configuration;

import io.galeb.api.handler.*;
import io.galeb.core.entity.VirtualHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

@Component
public class ApiRepositoryEventListener extends AbstractRepositoryEventListener {

    @Autowired
    public PoolHandler poolHandler;

    @Autowired
    public ProjectHandler projectHandler;

    @Autowired
    public RuleGroupHandler ruleGroupHandler;

    @Autowired
    public RuleHandler ruleHandler;

    @Autowired
    public TargetHandler targetHandler;

    @Autowired
    public VirtualHostHandler virtualHostHandler;

    @Override
    protected void onBeforeCreate(Object entity) {
        if (entity instanceof VirtualHost) {
            virtualHostHandler.beforeCreate((VirtualHost) entity);
        }
    }
}
