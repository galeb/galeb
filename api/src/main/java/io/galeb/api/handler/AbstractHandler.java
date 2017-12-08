package io.galeb.api.handler;

import io.galeb.core.entity.AbstractEntity;

public abstract class AbstractHandler<T extends AbstractEntity> {

    public void beforeCreate(T entity) {

    }

    public void afterCreate(T entity) {

    }

    public void beforeSave(T entity) {

    }

    public void afterSave(T entity) {

    }

    public void beforeDelete(T entity) {

    }

    public void afterDelete(T entity) {

    }


}
