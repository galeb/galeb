package io.galeb.router.configurations;

import io.galeb.router.handlers.RuleTargetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RuleTargetHandlerConfiguration {

    private final ApplicationContext context;

    @Autowired
    public RuleTargetHandlerConfiguration(final ApplicationContext context) {
        this.context = context;
    }

    @Bean
    @Scope("prototype")
    RuleTargetHandler ruleTargetHandler() {
        return new RuleTargetHandler(context);
    }
}
