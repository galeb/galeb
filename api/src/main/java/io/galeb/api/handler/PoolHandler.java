package io.galeb.api.handler;

import io.galeb.core.entity.Pool;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
public class PoolHandler extends AbstractHandler<Pool> {

}
