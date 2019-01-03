/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
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

package io.galeb.legba.conversors;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.JsonPath;
import io.galeb.core.entity.HealthStatus.Status;
import io.galeb.core.log.JsonEventToLogger;
import io.galeb.legba.controller.RoutersController.RouterMeta;
import io.galeb.legba.model.v1.VirtualHost;
import io.galeb.legba.model.v2.QueryResultLine;
import io.galeb.legba.repository.VirtualHostRepository;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import net.minidev.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConverterV1Test {

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

    enum State {
        INITIAL,
        STATE_1,
        STATE_2,
        STATE_3,
        STATE_4,
        STATE_5,
        STATE_6,
        STATE_7,
        STATE_8,
        STATE_9,
        STATE_10
    }

    private HashMap<State, ConverterV1> converterV1 = new HashMap<>();
    private Date dateOne = Date.from(Instant.now());
    private Date dateTwo = Date.from(dateOne.toInstant().minus(10,  ChronoUnit.MINUTES));

    private HashMap<AbstractMap.SimpleEntry<State, String>, Boolean> hashNotModified = new HashMap<AbstractMap.SimpleEntry<State, String>, Boolean>(){{
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost1"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost2"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost3"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost5"), true);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_5, "virtualhost1"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_5, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_5, "virtualhost3"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_5, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_5, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_6, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_6, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_6, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_6, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_6, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_7, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_7, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_7, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_7, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_7, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_8, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_8, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_8, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_8, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_8, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_9, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_9, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_9, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_9, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_9, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_10, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_10, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_10, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_10, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_10, "virtualhost5"), false);
    }};

    private HashMap<State, List<Object[]>> states = new HashMap<State, List<Object[]>>() {{
        // @formatter:off
        put(State.INITIAL, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", "NULL", Status.UNKNOWN.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", "NULL", Status.UNKNOWN.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", "NULL", Status.UNKNOWN.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", "NULL", Status.UNKNOWN.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", "NULL", Status.UNKNOWN.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", "NULL", Status.UNKNOWN.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", "NULL", Status.UNKNOWN.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", "NULL", Status.UNKNOWN.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", "NULL", Status.UNKNOWN.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", "NULL", Status.UNKNOWN.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", "NULL", Status.UNKNOWN.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", "NULL", Status.UNKNOWN.toString(), 9L, 9L},
        }));

        put(State.STATE_1, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", "NULL", Status.UNKNOWN.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", "NULL", Status.UNKNOWN.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateOne.toString(), Status.UNKNOWN.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", "NULL", Status.UNKNOWN.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.UNKNOWN.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.UNKNOWN.toString() + "," + Status.HEALTHY.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.UNKNOWN.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.UNKNOWN.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.FAIL.toString() + "," + Status.UNKNOWN.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.UNKNOWN.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 9L, 9L},
        }));

        put(State.STATE_2, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", "NULL", Status.UNKNOWN.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", "NULL", Status.UNKNOWN.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateOne.toString(), Status.UNKNOWN.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateTwo.toString(), Status.FAIL.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateTwo.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateTwo.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 9L, 9L},
        }));

        put(State.STATE_3, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.HEALTHY.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 9L},
        }));

        put(State.STATE_4, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.HEALTHY.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 9L},
        }));

        put(State.STATE_5, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.HEALTHY.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 9L},
        }));

        put(State.STATE_6, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateOne, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateOne, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateTwo, "virtualhost3", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.HEALTHY.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 9L},
        }));

        put(State.STATE_7, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateTwo, "virtualhost3", dateTwo, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.HEALTHY.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateTwo, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateTwo, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateOne, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 9L},
        }));

        put(State.STATE_8, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 0, dateTwo, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 1, dateTwo, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 2, dateTwo, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 0, dateTwo, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 1, dateTwo, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 2, dateTwo, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateTwo, "virtualhost3", dateTwo, 0, dateTwo, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.HEALTHY.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateTwo, 0, dateTwo, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateTwo, 0, dateTwo, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateOne, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 9L},
        }));

        put(State.STATE_9, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 0, dateTwo, false, "rule1", "/1", dateTwo, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 1, dateTwo, false, "rule2", "/2", dateTwo, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 2, dateTwo, false, "rule3", "/", dateTwo, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 0, dateTwo, false, "rule4", "/1", dateTwo, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 1, dateTwo, false, "rule5", "/2", dateTwo, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 2, dateTwo, false, "rule6", "/", dateTwo, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateTwo, "virtualhost3", dateTwo, 0, dateTwo, false, "rule7", "/", dateTwo, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.HEALTHY.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateTwo, 0, dateTwo, false, "rule8", "/", dateTwo, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateTwo, 0, dateTwo, false, "rule8", "/", dateTwo, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateTwo, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateTwo, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateTwo, "pool7", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.9", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 9L},
        }));

        put(State.STATE_10, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 0, dateTwo, false, "rule1", "/1", dateTwo, "pool1", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 1L, 1L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 1, dateTwo, false, "rule2", "/2", dateTwo, "pool2", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 2L, 2L},
        {BigInteger.valueOf(1L), dateTwo, "virtualhost1", dateTwo, 2, dateTwo, false, "rule3", "/", dateTwo, "pool3", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.3", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 3L, 3L},

        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 0, dateTwo, false, "rule4", "/1", dateTwo, "pool1", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 4L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 1, dateTwo, false, "rule5", "/2", dateTwo, "pool1", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 5L, 1L},
        {BigInteger.valueOf(2L), dateTwo, "virtualhost2", dateTwo, 2, dateTwo, false, "rule6", "/", dateTwo, "pool1", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 6L, 1L},

        {BigInteger.valueOf(3L), dateTwo, "virtualhost3", dateTwo, 0, dateTwo, false, "rule7", "/", dateTwo, "pool4", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.4", dateOne.toString(), Status.HEALTHY.toString(), 7L, 4L},

        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateTwo, 0, dateTwo, false, "rule8", "/", dateTwo, "pool5", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString(), 8L, 5L},
        {BigInteger.valueOf(4L), dateTwo, "virtualhost4", dateTwo, 0, dateTwo, false, "rule8", "/", dateTwo, "pool5", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 8L, 6L},

        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateTwo, "pool6", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.7", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString(), 9L, 7L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateTwo, "pool6", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.8", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 8L},
        {BigInteger.valueOf(5L), dateTwo, "virtualhost5", dateTwo, 0, dateTwo, false, "rule9", "/", dateTwo, "pool7", BigInteger.valueOf(1L), "default", dateTwo, "http://127.0.0.9", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString(), 9L, 9L},
        }));

        // @formatter:on
    }};

    @Before
    public void setup() {
        converterV1.clear();
        states.keySet().forEach(state -> {
            VirtualHostRepository virtualHostRepository = mock(VirtualHostRepository.class);
            when(virtualHostRepository.fullEntity(anyLong(), anyString())).thenReturn(states.get(state));
            when(virtualHostRepository.fullEntityZoneIdNull(anyLong())).thenReturn(states.get(state));
            converterV1.put(state, new ConverterV1(virtualHostRepository));
        });
    }

    @Test
    public void stateFlowTest() {
        final Iterator<State> iterator = EnumSet.allOf(State.class).iterator();
        while (iterator.hasNext()) {
            State currentState = iterator.next();
            if (iterator.hasNext()) {
                State nextState = iterator.next();
                JsonEventToLogger event = new JsonEventToLogger(this.getClass());
                event.put("event", "ConverterV1Test.stateFlowTest: state " + currentState + " to " + nextState);
                event.sendInfo();
                checkStateChanges(currentState, nextState);
            }
        }
    }

    @Test
    public void convertJsonTest() {
        for (State currentState : EnumSet.allOf(State.class)) {
            JsonEventToLogger event = new JsonEventToLogger(this.getClass());
            event.put("event", "ConverterV1Test.convertJsonTest: state " + currentState);
            event.sendInfo();
            convertFrom(currentState);
        }
    }

    private void checkStateChanges(State currentState, State nextState) {
        final List<QueryResultLine> queryResultLinesBefore = converterV1.get(currentState).getQueryResultLines(states.get(currentState));
        final List<QueryResultLine> queryResultLinesAfter = converterV1.get(nextState).getQueryResultLines(states.get(nextState));
        Map<VirtualHost, String> lastMapBefore = new HashMap<>();
        Map<VirtualHost, String> lastMapAfter = new HashMap<>();

        final Map<VirtualHost, String> virtualhostFullHashBefore = new HashMap<>();
        final Map<VirtualHost, String> virtualhostFullHashAfter = new HashMap<>();

        for (QueryResultLine queryResultLine: queryResultLinesBefore) {
            VirtualHost v1 = new VirtualHost();
            v1.setName(queryResultLine.getVirtualhostName());
            lastMapBefore = converterV1.get(currentState).calculeHash(v1, queryResultLine, virtualhostFullHashBefore, 0);
        }
        for (QueryResultLine queryResultLine: queryResultLinesAfter) {
            VirtualHost v1 = new VirtualHost();
            v1.setName(queryResultLine.getVirtualhostName());
            lastMapAfter = converterV1.get(nextState).calculeHash(v1, queryResultLine, virtualhostFullHashAfter, 0);
        }
        for (VirtualHost virtualHost: lastMapBefore.keySet()) {
            String hashBefore = converterV1.get(currentState).makeHash(lastMapBefore.get(virtualHost));
            String hashAfter = converterV1.get(currentState).makeHash(lastMapAfter.get(virtualHost));
            if (hashNotModified.get(new <State, String> AbstractMap.SimpleEntry<State, String>(nextState, virtualHost.getName()))) {
                Assert.assertEquals(virtualHost.getName() + " is NOT equals (" + hashBefore + " != " + hashAfter + ")", hashBefore, hashAfter);
            } else {
                Assert.assertNotEquals(virtualHost.getName() + " IS equals (" + hashBefore + " == " + hashAfter + ")", hashBefore, hashAfter);
            }
        }
    }

    private String convertToString(State state) {
        RouterMeta routerMeta = new RouterMeta();
        routerMeta.envId = "1";
        routerMeta.groupId = "local";
        routerMeta.zoneId = "zone1";
        routerMeta.correlation = UUID.randomUUID().toString();
        int numRouters = 2;
        String version = "1";

        String jsonStr = converterV1.get(state).convertToString(routerMeta, numRouters, version);
        try {
            MAPPER.readTree(jsonStr);
            return jsonStr;
        } catch (IOException ignore) {
            Assert.fail("NOT JSON");
        }
        return null;
    }

    private void convertFrom(State state) {
        String jsonStr = convertToString(state);
        if (jsonStr != null) {
            int numVirtualhosts = ((JSONArray) JsonPath.read(jsonStr, "$.virtualhosts[*]")).size();
            IntStream.range(0, numVirtualhosts).forEach(pos -> {
                String virtualhostName = JsonPath.read(jsonStr, "$.virtualhosts[" + pos + "].name");
                long numTargetsOrigin = states.get(state).stream().filter(line -> virtualhostName.equals(line[2]) &&
                    (converterV1.get(state).canSendTargetToRoute((String)line[16]))).count();
                long numTargetsJson = ((JSONArray) JsonPath.read(jsonStr, "$.virtualhosts[" + pos + "].rules[*].pool.targets[*]")).size();
                Assert.assertEquals("virtualhost " + virtualhostName + " target count problem", numTargetsOrigin, numTargetsJson);
            });
        }
    }

}
