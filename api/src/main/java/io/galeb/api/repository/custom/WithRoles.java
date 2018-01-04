package io.galeb.api.repository.custom;

public interface WithRoles {

    boolean hasPermission(Object principal, Object criteria, String role);

}
