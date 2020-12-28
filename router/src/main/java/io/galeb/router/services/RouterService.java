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

package io.galeb.router.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import io.undertow.Undertow;

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

    @PreDestroy
    public void onDestroy() throws Exception {
        logger.info(this.getClass().getSimpleName() + " stoping");
        this.undertow.stop();
    }
}
