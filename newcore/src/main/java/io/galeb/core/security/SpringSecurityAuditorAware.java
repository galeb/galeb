package io.galeb.core.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    public String getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = "anonymousUser";

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                currentUser = ((UserDetails)principal).getUsername();
            } else {
                currentUser = principal.toString();
            }
        }

        return currentUser;
    }
}
