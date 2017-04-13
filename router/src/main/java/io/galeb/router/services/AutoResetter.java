/**
 *
 */

package io.galeb.router.services;

import io.galeb.router.handlers.RootHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.Objects;

import static io.galeb.router.services.ExternalData.*;

@Service
public class AutoResetter {

    private static final String FORCE_UPDATE_FLAG = "/force_update";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RootHandler rootHandler;
    private final ExternalData data;

    @Autowired
    public AutoResetter(final RootHandler rootHandler, final ExternalData externalData) {
        this.rootHandler = rootHandler;
        this.data = externalData;
    }

    @PostConstruct
    public void run() {
        logger.info(this.getClass().getSimpleName() + " started");
    }

    @Scheduled(fixedRate = 5000)
    public void checkForceUpdateFlag() {
        if (forceUpdateAll()) return;
        forceUpdateByVirtualhost();
        forceUpdateByPool();
    }

    private void forceUpdateByVirtualhost() {
        data.listFrom(VIRTUALHOSTS_KEY).stream()
                .filter(node -> data.exist(node.getKey() + FORCE_UPDATE_FLAG))
                .map(node -> data.node(node.getKey() + FORCE_UPDATE_FLAG).getValue())
                .filter(Objects::nonNull)
                .forEach(rootHandler::forceVirtualhostUpdate);
    }

    private void forceUpdateByPool() {
        data.listFrom(POOLS_KEY).stream()
                .filter(node -> data.exist(node.getKey() + FORCE_UPDATE_FLAG))
                .map(node -> data.node(node.getKey() + FORCE_UPDATE_FLAG).getValue())
                .filter(Objects::nonNull)
                .forEach(rootHandler::forcePoolUpdate);
    }

    private boolean forceUpdateAll() {
        if (data.exist(PREFIX_KEY + FORCE_UPDATE_FLAG)) {
            rootHandler.forceAllUpdate();
            return true;
        }
        return false;
    }

}
