package io.galeb.api.repository.custom;

import java.util.Set;

public interface WithRoles {

    Set<String> roles(Object principal, Object criteria);

}
