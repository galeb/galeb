package io.galeb.core.entity;

public interface WithStatus {

    enum Status {
        PENDING,
        OK,
        UNKNOWN
    }

    Status getStatus();

}
