package io.galeb.api.handler;

import io.galeb.core.entity.Rule;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler
public class RuleHandler extends AbstractHandler<Rule> {
}
