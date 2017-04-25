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

import com.timgroup.statsd.NonBlockingStatsDClient;
import io.galeb.core.configuration.SystemEnvs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StatsdClient {

    private static final String STATSD_PREFIX = SystemEnvs.STATSD_PREFIX.getValue();
    private static final String STATSD_HOST   = SystemEnvs.STATSD_HOST.getValue();
    private static final int    STATSD_PORT   = Integer.parseInt(SystemEnvs.STATSD_PORT.getValue());

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NonBlockingStatsDClient client = new NonBlockingStatsDClient(STATSD_PREFIX, STATSD_HOST, STATSD_PORT);

    public void incr(String metricName, int step, double rate) {
        count(metricName, step, rate);
    }

    public void incr(String metricName) {
        incr(metricName, 1, 1.0);
    }

    public void incr(String metricName, int step) {
        incr(metricName, step, 1.0);
    }

    public void incr(String metricName, double rate) {
        incr(metricName, 1, rate);
    }

    public void decr(String metricName, int step, double rate) {
        client.count(metricName, -1L * step, rate);
    }

    public void decr(String metricName) {
        decr(metricName, 1, 1.0);
    }

    public void decr(String metricName, int step) {
        decr(metricName, step, 1.0);
    }

    public void decr(String metricName, double rate) {
        decr(metricName, 1, rate);
    }

    public void count(String metricName, int value, double rate) {
        client.count(metricName, value, rate);
    }

    public void count(String metricName, int value) {
        count(metricName, value, 1.0);
    }

    public void gauge(String metricName, double value, double rate) {
        client.recordGaugeValue(metricName, value);
    }

    public void gauge(String metricName, double value) {
        gauge(metricName, value, 1.0);
    }

    public void set(String metricName, String value, double rate) {
        client.recordSetEvent(metricName, value);
    }

    public void set(String metricName, String value) {
        set(metricName, value, 1.0);
    }

    public void timing(String metricName, long value, double rate) {
        client.recordExecutionTime(metricName, value, rate);
    }

    public void timing(String metricName, long value) {
        timing(metricName, value, 1.0);
    }
}
