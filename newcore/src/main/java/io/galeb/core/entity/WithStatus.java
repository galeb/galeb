package io.galeb.core.entity;

public interface WithStatus {

    void setStatus(Status status);

    enum Status {
        PENDING,
        OK,
        UNKNOWN
    }

    Status getStatus();

}
