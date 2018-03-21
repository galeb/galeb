package io.galeb.api.security;

import io.galeb.api.services.LocalAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class InMemoryAuthenticatorProvider  extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private CurrentUserDetailsService currentUserDetailsService;

    @Autowired
    private LocalAdminService localAdmin;


    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        boolean isAdmin = LocalAdminService.NAME.equals(authentication.getName()) && localAdmin.check((String) authentication.getCredentials());
        if (isAdmin) return new UsernamePasswordAuthenticationToken(localAdmin, authentication.getCredentials(), localAdmin.getAuthorities());
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