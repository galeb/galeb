package io.galeb.api.repository.custom;

import java.util.Collections;
import java.util.Set;

public class AccountRepositoryImpl implements AccountRepositoryCustom {

    @Override
    public Set<String> roles(Object principal, Object criteria) {
        return Collections.emptySet();
    }

}
