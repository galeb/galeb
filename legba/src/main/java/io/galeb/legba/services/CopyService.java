package io.galeb.legba.services;

import io.galeb.core.entity.VirtualHost;
import io.galeb.legba.repository.VirtualHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopyService {

    @Autowired
    private VirtualHostRepository virtualHostRepository;

    public List<VirtualHost> getVirtualHosts(Long envId) {
        List<VirtualHost> listVirtualHost;
        listVirtualHost = virtualHostRepository.findAllByEnvironmentId(envId);
        listVirtualHost.stream().forEach(vh -> {
            vh.getEnvironments();
            vh.getVirtualhostgroup().getRulesordered().stream().forEach(ro -> {
                ro.getRule().getPools().stream().forEach(p -> {
                    p.getBalancepolicy();
                    p.getHcHeaders();
                    p.getTargets().stream().forEach( t -> {
                        t.getHealthStatus().stream().forEach(hs -> {});
                    });
                });
            });
        });
        return listVirtualHost;
    }

}
