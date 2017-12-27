package io.galeb.core.entity;

import java.util.Map;

public interface WithStatus {

    enum Status {
        PENDING,
        OK,
        UNKNOWN,
        DELETED
    }

    Map<Long, Status> getStatus();
    void setStatus(Map<Long, Status> status);

}
