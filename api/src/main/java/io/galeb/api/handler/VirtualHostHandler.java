package io.galeb.api.handler;

import io.galeb.core.entity.VirtualHost;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
public class VirtualHostHandler extends AbstractHandler<VirtualHost> {
}
