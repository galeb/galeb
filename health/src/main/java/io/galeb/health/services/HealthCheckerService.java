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

import io.galeb.health.SystemEnvs;
import io.galeb.health.broker.Checker;
import io.galeb.health.broker.Producer;
import io.galeb.health.externaldata.TargetHealth;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static io.galeb.health.utils.ErrorLogger.logError;

@Service
public class HealthCheckerService {

    private final String environmentName = SystemEnvs.ENVIRONMENT_NAME.getValue();

    @SuppressWarnings("FieldCanBeLocal")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Producer producer;
    private final TargetHealth targetHealth;

    HealthCheckerService(final Producer producer, final TargetHealth targetHealth) {
        this.producer = producer;
        this.targetHealth = targetHealth;
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
            targetHealth.targetsByEnvName(environmentName).parallel().forEach(target -> {
                try {
                    target.getProperties().put("SCHEDULER_ID", id);
                    producer.send(target);
                } catch (Exception e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            });
        } catch (Exception e) {
            logError(e, this.getClass());
        }
        logger.info("Finished scheduling " + id);
    }
}
