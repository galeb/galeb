package io.galeb.core.entity;

public interface WithParent<T extends AbstractEntity<?>> {

    T getParent();

}
