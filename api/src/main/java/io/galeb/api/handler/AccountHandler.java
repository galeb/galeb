package io.galeb.api.handler;

import io.galeb.core.entity.Account;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.UUID;

import static com.google.common.hash.Hashing.sha256;

@Component
public class AccountHandler extends AbstractHandler<Account> {

    @Override
    protected void onBeforeSave(Account account) {
        super.onBeforeSave(account);
        if (account.getResettoken() != null && account.getResettoken()) {
            account.setApitoken(sha256().hashString(UUID.randomUUID().toString(), Charset.defaultCharset()).toString());
            account.setResettoken(false);
        }
    }
}
