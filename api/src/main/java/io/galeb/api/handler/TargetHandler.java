package io.galeb.api.handler;

import io.galeb.core.entity.Target;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
public class TargetHandler extends AbstractHandler<Target> {
}
