package io.galeb.api.security;

import io.galeb.core.entity.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class ApiTokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final Logger LOGGER = LogManager.getLogger(ApiTokenAuthenticationProvider.class);

    @Autowired
    private CurrentUserDetailsService currentUserDetailsService;

    @Autowired
    private LocalAdmin localAdmin;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getPrincipal() == null) {
            throw new SecurityException("principal is NULL");
        }
        final UserDetails userDetails = retrieveUser(authentication.getName(), null);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, authentication.getCredentials(), userDetails.getAuthorities());
        if ((LocalAdmin.NAME.equals(authentication.getName()) && localAdmin.check((String) authentication.getCredentials())) ||
                ((Account) userDetails).getApitoken().equals(authentication.getCredentials())) {
            return token;
        }
        throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        return currentUserDetailsService.loadUserByUsername(username);
    }

    @Override
    public boolean supports(Class<?> authenticationClass) {
        return Authentication.class.isAssignableFrom(authenticationClass);
    }
}
