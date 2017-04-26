/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.health.services;

import io.galeb.core.configuration.SystemEnvs;
import io.galeb.core.entity.Target;
import io.galeb.core.rest.ManagerClient;
import io.galeb.health.broker.Checker;
import io.galeb.health.broker.Producer;
import io.galeb.health.util.TargetStamper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

import static io.galeb.core.logger.ErrorLogger.logError;

@Service
public class HealthCheckerService {

    @SuppressWarnings("FieldCanBeLocal")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Producer producer;
    private final ManagerClient managerClient;

    @Autowired
    HealthCheckerService(final Producer producer, final ManagerClient managerClient) {
        this.producer = producer;
        this.managerClient = managerClient;
        logger.info(this.getClass().getSimpleName() + " started");
    }

    @Scheduled(fixedRate = 10000)
    public void getTargetsAndSendToQueue() {
        if (Checker.LAST_CALL.get() + 5000L >= System.currentTimeMillis()) {
            return;
        }
        String id = UUID.randomUUID().toString();
        logger.info("Running scheduling " + id);
        try {
            ManagerClient.ResultCallBack resultCallBack = result -> {
                @SuppressWarnings("unchecked")
                Set<Target> targets = (Set<Target>) result;
                targets.parallelStream().parallel().forEach(target -> {
                    try {
                        target.getProperties().put("SCHEDULER_ID", id);
                        producer.send(target);
                    } catch (Exception e) {
                        logger.error(ExceptionUtils.getStackTrace(e));
                    }
                });
            };
            managerClient.getTargets(resultCallBack);
        } catch (Exception e) {
            logError(e, this.getClass());
        }
        logger.info("Finished scheduling " + id);
    }
}
