package io.galeb.api.handler;

import io.galeb.core.entity.AbstractEntity;
import io.galeb.core.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectHandler extends AbstractHandler<Project> {

    @Override
    public Class<? extends AbstractEntity> entityClass() {
        return Project.class;
    }
}
