package io.galeb.api.resourcesprocessor;

import io.galeb.core.entity.Target;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
public class PagedTargetResourceProcessor implements ResourceProcessor<PagedResources<Resource<Target>>> {

    @Override
    public PagedResources<Resource<Target>> process(PagedResources<Resource<Target>> resource) {
        return new PagedResources<>(resource.getContent(), resource.getMetadata());
    }
}
