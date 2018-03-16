package io.galeb.api.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SpringRestConfiguration extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        setupCors(config);
        exposeIdsEntities(config);
    }

    private void exposeIdsEntities(RepositoryRestConfiguration config) {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

        final Set<BeanDefinition> beans = provider.findCandidateComponents("io.galeb.core.entity");

        for (BeanDefinition bean : beans) {
            try {
                Class<?> idExposedClasses = Class.forName(bean.getBeanClassName());
                config.exposeIdsFor(Class.forName(idExposedClasses.getName()));
            } catch (ClassNotFoundException e) {
                // Can't throw ClassNotFoundException due to the method signature. Need to cast it
                throw new RuntimeException("Failed to expose `id` field due to", e);
            }
        }
    }

    private void setupCors(RepositoryRestConfiguration config) {
        String pathPatternCors = "/**";
        config.getCorsRegistry().addMapping(pathPatternCors);
        CorsConfiguration corsConfiguration = config.getCorsRegistry().getCorsConfigurations().get(pathPatternCors);
        corsConfiguration.addAllowedMethod("PATCH");
        corsConfiguration.addAllowedMethod("DELETE");
    }
}
