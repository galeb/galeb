package io.galeb.core.entity;

public interface WithFarmID<T extends AbstractEntity<?>> {

    long getFarmId();

    T setFarmId(long farmId);

}
