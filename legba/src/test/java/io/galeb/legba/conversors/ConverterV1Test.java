package io.galeb.legba.conversors;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.JsonPath;
import io.galeb.core.entity.HealthStatus.Status;
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
import java.util.HashMap;
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
        STATE_5
    }

    private HashMap<State, ConverterV1> converterV1 = new HashMap<>();
    private Date dateOne = Date.from(Instant.now());
    private Date dateTwo = Date.from(dateOne.toInstant().minus(10,  ChronoUnit.MINUTES));
    private Date nullDate = null;

    private HashMap<AbstractMap.SimpleEntry<State, String>, Boolean> hashNotModified = new HashMap<AbstractMap.SimpleEntry<State, String>, Boolean>(){{
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost1"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_1, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost3"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_2, "virtualhost5"), false);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost1"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost2"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost3"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost4"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_3, "virtualhost5"), true);

        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost1"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost2"), false);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost3"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost4"), true);
        put(new <State, String> AbstractMap.SimpleEntry<State, String>(State.STATE_4, "virtualhost5"), true);
    }};

    private HashMap<State, List<Object[]>> states = new HashMap<State, List<Object[]>>() {{
        // @formatter:off
        put(State.INITIAL, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", nullDate, null},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", nullDate, null},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", nullDate, null},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", nullDate, null},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule5", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", nullDate, null},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule6", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", nullDate, null},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", nullDate, null},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", nullDate, null},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule8", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", nullDate, null},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", nullDate, null},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule9", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", nullDate, null},
        }));

        put(State.STATE_1, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", nullDate, null},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", nullDate, null},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", nullDate, null},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule4", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule4", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule5", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateOne.toString(), Status.FAIL.toString()},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule6", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule6", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString()},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateOne.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},
        }));

        put(State.STATE_2, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule4", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString()},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule4", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule5", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateTwo.toString(), Status.HEALTHY.toString()},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule6", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString()},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule6", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString()},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        }));

        put(State.STATE_3, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.FAIL.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule4", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.FAIL.toString()},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule4", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.FAIL.toString() + "," + Status.FAIL.toString()},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule5", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateTwo.toString(), Status.HEALTHY.toString()},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule6", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule6", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        }));

        put(State.STATE_4, Arrays.asList(new Object[][]{
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 0, dateOne, false, "rule1", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 1, dateOne, false, "rule2", "/2", dateOne, "pool2", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.2", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(1L), dateOne, "virtualhost1", dateOne, 2, dateOne, false, "rule3", "/", dateOne, "pool3", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.3", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},

        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 0, dateOne, false, "rule4", "/1", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 1, dateOne, false, "rule4", "/2", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(2L), dateOne, "virtualhost2", dateOne, 2, dateOne, false, "rule4", "/", dateOne, "pool1", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.1", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},

        {BigInteger.valueOf(3L), dateOne, "virtualhost3", dateOne, 0, dateOne, false, "rule5", "/", dateOne, "pool4", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.4", dateTwo.toString(), Status.HEALTHY.toString()},

        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule6", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.5", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(4L), dateOne, "virtualhost4", dateOne, 0, dateOne, false, "rule6", "/", dateOne, "pool5", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.6", dateOne.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},

        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.7", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
        {BigInteger.valueOf(5L), dateOne, "virtualhost5", dateOne, 0, dateOne, false, "rule7", "/", dateOne, "pool6", BigInteger.valueOf(1L), "default", dateOne, "http://127.0.0.8", dateTwo.toString(), Status.HEALTHY.toString() + "," + Status.HEALTHY.toString()},
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
            lastMapBefore = converterV1.get(currentState).calculeHash(v1, queryResultLine, virtualhostFullHashBefore);
        }
        for (QueryResultLine queryResultLine: queryResultLinesAfter) {
            VirtualHost v1 = new VirtualHost();
            v1.setName(queryResultLine.getVirtualhostName());
            lastMapAfter = converterV1.get(nextState).calculeHash(v1, queryResultLine, virtualhostFullHashAfter);
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

    @Test
    public void stateInitialToState2() {
        checkStateChanges(State.INITIAL, State.STATE_1);
    }

    @Test
    public void state1ToState2() {
        checkStateChanges(State.STATE_1, State.STATE_2);
    }

    @Test
    public void state2ToState3() {
        checkStateChanges(State.STATE_2, State.STATE_3);
    }

    @Test
    public void state3ToState4() {
        checkStateChanges(State.STATE_3, State.STATE_4);
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

    @Test
    public void convertFromState1() {
        String jsonStr = convertToString(State.STATE_1);
        if (jsonStr != null) {
            int numVirtualhosts = ((JSONArray) JsonPath.read(jsonStr, "$.virtualhosts[*]")).size();
            IntStream.range(0, numVirtualhosts).forEach(pos -> {
                String virtualhostName = JsonPath.read(jsonStr, "$.virtualhosts[" + pos + "].name");
                System.out.println(virtualhostName);
                long numTargetsOrigin = states.get(State.STATE_1).stream().filter(line -> virtualhostName.equals(line[2])).count();
                long numTargetsJson = ((JSONArray) JsonPath.read(jsonStr, "$.virtualhosts[" + pos + "].rules[*].pool.targets[*]")).size();
                Assert.assertEquals("virtualhost " + virtualhostName + " target count problem", numTargetsOrigin, numTargetsJson);
            });
        }
    }

    @Test
    public void convertFromState4() {
        String jsonStr = convertToString(State.STATE_4);
        if (jsonStr != null) {
            int numVirtualhosts = ((JSONArray) JsonPath.read(jsonStr, "$.virtualhosts[*]")).size();
            IntStream.range(0, numVirtualhosts).forEach(pos -> {
                String virtualhostName = JsonPath.read(jsonStr, "$.virtualhosts[" + pos + "].name");
                System.out.println(virtualhostName);
                long numTargetsOrigin = states.get(State.STATE_4).stream().filter(line -> virtualhostName.equals(line[2])).count();
                long numTargetsJson = ((JSONArray) JsonPath.read(jsonStr, "$.virtualhosts[" + pos + "].rules[*].pool.targets[*]")).size();
                Assert.assertEquals("virtualhost " + virtualhostName + " target count problem", numTargetsOrigin, numTargetsJson);
            });
        }
    }

}
