package io.galeb.api.repository.custom;

public class AccountRepositoryImpl implements AccountRepositoryCustom {

    @Override
    public boolean hasPermission(Object principal, Object criteria, String role) {
        return true;
    }

}
