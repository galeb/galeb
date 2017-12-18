package io.galeb.api.handler;

import io.galeb.core.entity.AbstractEntity;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;

@SuppressWarnings("unused")
public abstract class AbstractHandler<T extends AbstractEntity> extends AbstractRepositoryEventListener<T> {

    @Override
    protected void onBeforeCreate(T entity) {
        super.onBeforeCreate(entity);
    }

    @Override
    protected void onAfterCreate(T entity) {
        super.onAfterCreate(entity);
    }

    @Override
    protected void onBeforeSave(T entity) {
        super.onBeforeSave(entity);
    }

    @Override
    protected void onAfterSave(T entity) {
        super.onAfterSave(entity);
    }

    @Override
    protected void onBeforeLinkSave(T parent, Object linked) {
        super.onBeforeLinkSave(parent, linked);
    }

    @Override
    protected void onAfterLinkSave(T parent, Object linked) {
        super.onAfterLinkSave(parent, linked);
    }

    @Override
    protected void onBeforeLinkDelete(T parent, Object linked) {
        super.onBeforeLinkDelete(parent, linked);
    }

    @Override
    protected void onAfterLinkDelete(T parent, Object linked) {
        super.onAfterLinkDelete(parent, linked);
    }

    @Override
    protected void onBeforeDelete(T entity) {
        super.onBeforeDelete(entity);
    }

    @Override
    protected void onAfterDelete(T entity) {
        super.onAfterDelete(entity);
    }
}
