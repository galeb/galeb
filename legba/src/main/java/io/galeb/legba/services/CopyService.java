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
        return listVirtualHost;
    }

}
