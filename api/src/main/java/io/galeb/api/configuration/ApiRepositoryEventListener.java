package io.galeb.api.configuration;

import io.galeb.api.handler.AbstractHandler;
import io.galeb.api.handler.PoolHandler;
import io.galeb.api.handler.VirtualHostHandler;
import io.galeb.core.entity.AbstractEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
@Component
public class ApiRepositoryEventListener extends AbstractRepositoryEventListener {

    private final Set<AbstractHandler> handlers = new HashSet<>();

    @Autowired
    public PoolHandler poolHandler;

    @Autowired
    public VirtualHostHandler projectHandler;

    @Autowired
    public VirtualHostHandler ruleGroupHandler;

    @Autowired
    public VirtualHostHandler ruleHandler;

    @Autowired
    public VirtualHostHandler targetHandler;

    @Autowired
    public VirtualHostHandler virtualHostHandler;

    @PostConstruct
    public void init() {
        AbstractHandler arrayHandlers[] = { virtualHostHandler, projectHandler, ruleGroupHandler, ruleHandler, targetHandler, virtualHostHandler };
        Collections.addAll(handlers, arrayHandlers);
    }

    @Override
    protected void onBeforeCreate(Object entity) {
        handlers.forEach(h -> {
            System.out.println(h.entityClass());
            if (h.entityClass().isInstance(entity)) h.beforeCreate((AbstractEntity) entity);
        });
    }

    @Override
    protected void onAfterCreate(Object entity) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(entity)) h.afterCreate((AbstractEntity) entity);
        });
    }

    @Override
    protected void onBeforeSave(Object entity) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(entity)) h.beforeSave((AbstractEntity) entity);
        });
    }

    @Override
    protected void onAfterSave(Object entity) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(entity)) h.afterSave((AbstractEntity) entity);
        });
    }

    @Override
    protected void onBeforeLinkSave(Object parent, Object linked) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(linked)) h.beforeLinkSave(parent, (AbstractEntity) linked);
        });
    }

    @Override
    protected void onAfterLinkSave(Object parent, Object linked) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(linked)) h.afterLinkSave(parent, (AbstractEntity) linked);
        });
    }

    @Override
    protected void onBeforeLinkDelete(Object parent, Object linked) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(linked)) h.beforeLinkDelete(parent, (AbstractEntity) linked);
        });
    }

    @Override
    protected void onAfterLinkDelete(Object parent, Object linked) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(linked)) h.afterLinkDelete(parent, (AbstractEntity) linked);
        });
    }

    @Override
    protected void onBeforeDelete(Object entity) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(entity)) h.beforeDelete((AbstractEntity) entity);
        });
    }

    @Override
    protected void onAfterDelete(Object entity) {
        handlers.forEach(h -> {
            if (h.entityClass().isInstance(entity)) h.afterDelete((AbstractEntity) entity);
        });
    }
}
