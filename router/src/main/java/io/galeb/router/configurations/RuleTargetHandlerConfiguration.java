package io.galeb.router.configurations;

import io.galeb.router.handlers.RuleTargetHandler;
import io.galeb.router.services.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RuleTargetHandlerConfiguration {

    private final ApplicationContext context;
    private final ExternalDataService data;

    @Autowired
    public RuleTargetHandlerConfiguration(final ApplicationContext context, final ExternalDataService externalData) {
        this.context = context;
        this.data = externalData;
    }

    @Bean
    @Scope("prototype")
    RuleTargetHandler ruleTargetHandler() {
        return new RuleTargetHandler(context, data);
    }
}
