package io.galeb.api.handler;

import io.galeb.core.entity.Account;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.google.common.hash.Hashing.sha256;

@Component
public class AccountHandler extends AbstractHandler<Account> {

    @Override
    protected void onBeforeSave(Account account) {
        super.onBeforeSave(account);
        if (account.getRenewtoken() != null && account.getRenewtoken()) {
            account.setApitoken(sha256().hashBytes((UUID.randomUUID().toString()).getBytes()).toString());
            account.setRenewtoken(false);
        }
    }
}
