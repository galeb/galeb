/**
 *
 */

package io.galeb.router.services;

import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Order(3)
public class RouterService {

    private final Logger   logger = LoggerFactory.getLogger(this.getClass());
    private final Undertow undertow;

    @Autowired
    public RouterService(final Undertow undertow) {
        this.undertow = undertow;
    }

    @PostConstruct
    public void run() {
        logger.info(this.getClass().getSimpleName() + " started");
        undertow.start();
    }
}
