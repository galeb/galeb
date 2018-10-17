package io.galeb.api.resourcesprocessor;

import io.galeb.core.entity.Target;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
public class TargetResourceProcessor implements ResourceProcessor<Resource<Target>> {

    @Override
    public Resource<Target> process(Resource<Target> resource) {
        return new Resource<>(resource.getContent());
    }
}
