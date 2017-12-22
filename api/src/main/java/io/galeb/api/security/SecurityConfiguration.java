package io.galeb.api.security;

import io.galeb.api.security.filter.InMemoryAccountFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityEvaluationContextExtension securityExtension() {
        return new SecurityEvaluationContextExtension();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(new InMemoryAccountFilter(), BasicAuthenticationFilter.class);
        http.authorizeRequests().anyRequest().authenticated().and().httpBasic();
        http.csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(((CurrentUserDetailsService)userDetailsService).localAdmin().getUsername())
                .password(((CurrentUserDetailsService)userDetailsService).localAdmin().getApitoken()).roles("USER");
        auth.authenticationProvider(authenticationProvider);
        auth.userDetailsService(userDetailsService);
    }
}
