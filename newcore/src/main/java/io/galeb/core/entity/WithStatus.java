package io.galeb.core.entity;

public interface WithStatus {

    enum Status {
        PENDING,
        OK,
        UNKNOWN
    }

    default Status getStatus() {
        return Status.UNKNOWN;
    }

}
