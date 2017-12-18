package io.galeb.api.handler;

import io.galeb.core.entity.AbstractEntity;

@SuppressWarnings("unused")
public abstract class AbstractHandler<T extends AbstractEntity> {

    public void beforeCreate(T entity) {

    }

    public void afterCreate(T entity) {

    }

    public void beforeSave(T entity) {

    }

    public void afterSave(T entity) {

    }

    public void beforeLinkSave(Object parent, T linked) {

    }

    public void afterLinkSave(Object parent, T linked) {

    }

    public void beforeLinkDelete(Object parent, T linked) {

    }

    public void afterLinkDelete(Object parent, T linked) {

    }

    public void beforeDelete(T entity) {

    }

    public void afterDelete(T entity) {

    }

    public abstract Class<? extends AbstractEntity> entityClass();

}
