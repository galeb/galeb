package io.galeb.api.handler;

import io.galeb.api.repository.VirtualhostGroupRepository;
import io.galeb.core.entity.Environment;
import io.galeb.core.entity.VirtualHost;
import io.galeb.core.entity.VirtualhostGroup;
import io.galeb.core.entity.WithStatus;
import io.galeb.core.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
public class VirtualHostHandler extends AbstractHandler<VirtualHost> {

    @Autowired
    VirtualhostGroupRepository virtualhostGroupRepository;

    @Override
    protected void onBeforeCreate(VirtualHost virtualHost) {
        super.onBeforeCreate(virtualHost);
        if (virtualHost.getEnvironments() == null || virtualHost.getEnvironments().isEmpty()) {
            throw new BadRequestException("Environment(s) undefined");
        }
        if (virtualHost.getVirtualhostgroup() == null) {
            VirtualhostGroup virtualhostGroup = new VirtualhostGroup();
            virtualhostGroupRepository.save(virtualhostGroup);
            virtualHost.setVirtualhostgroup(virtualhostGroup);
        }
        virtualHost.setStatus(WithStatus.Status.OK);
    }

    @Override
    protected Set<Environment> getAllEnvironments(VirtualHost entity) {
        return entity.getEnvironments();
    }

}
