package io.galeb.api.configuration;

import java.util.stream.Collectors;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.hateoas.core.DefaultRelProvider;
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
        disableEvo(config);
    }

    private void disableEvo(RepositoryRestConfiguration config) {
        config.setRelProvider(new DefaultRelProvider());
    }

    private void exposeIdsEntities(RepositoryRestConfiguration config) {
        final Set<BeanDefinition> beans = allBeansDomain();
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

    private Set<BeanDefinition> allBeansDomain() {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        return provider.findCandidateComponents("io.galeb.core.entity");
    }

    public Set<? extends Class<?>> allEntitiesClass() {
        return allBeansDomain().stream().map(b -> {
            try {
                return Class.forName(Class.forName(b.getBeanClassName()).getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toSet());
    }

    private void setupCors(RepositoryRestConfiguration config) {
        String pathPatternCors = "/**";
        config.getCorsRegistry().addMapping(pathPatternCors);
        CorsConfiguration corsConfiguration = config.getCorsRegistry().getCorsConfigurations().get(pathPatternCors);
        corsConfiguration.addAllowedMethod("PATCH");
        corsConfiguration.addAllowedMethod("DELETE");
    }
}
