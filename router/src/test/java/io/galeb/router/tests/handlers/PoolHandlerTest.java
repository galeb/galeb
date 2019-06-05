package io.galeb.router.tests.handlers;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.xnio.OptionMap;
import org.xnio.Options;

import io.galeb.core.entity.Pool;
import io.galeb.router.handlers.PoolHandler;

public class PoolHandlerTest {
    
    @Test
    public void testGetUndertowOptionMapWithOptionKeepAlive() throws Exception {
        HashMap<String, String> env = new HashMap<String, String>();
        env.put("UNDERTOW_OPTIONS_KEEP_ALIVE", "true");
        
        Pool pool = new Pool("pool-test");
        PoolHandler poolHandler = new PoolHandler(pool);
        
        OptionMap optionMap = poolHandler.getUndertowOptionMap(env, "UNDERTOW_OPTIONS_");
        
        Assert.assertEquals(optionMap.get(Options.KEEP_ALIVE, false), true);
    }
    
    @Test
    public void testGetUndertowOptionMapWithTwoBooleanOptions() throws Exception {
        HashMap<String, String> env = new HashMap<String, String>();
        env.put("UNDERTOW_OPTIONS_KEEP_ALIVE", "true");
        env.put("UNDERTOW_OPTIONS_ALLOW_BLOCKING", "true");
        
        Pool pool = new Pool("pool-test");
        PoolHandler poolHandler = new PoolHandler(pool);
        
        OptionMap optionMap = poolHandler.getUndertowOptionMap(env, "UNDERTOW_OPTIONS_");
        
        Assert.assertEquals(optionMap.get(Options.KEEP_ALIVE, false), true);
        Assert.assertEquals(optionMap.get(Options.ALLOW_BLOCKING, false), true);
    }
    
    @Test
    public void testGetUndertowOptionMapWithBooleanOptionAndIntegerOption() throws Exception {
        HashMap<String, String> env = new HashMap<String, String>();
        env.put("UNDERTOW_OPTIONS_KEEP_ALIVE", "true");
        env.put("UNDERTOW_OPTIONS_BALANCING_CONNECTIONS", "100");
        
        Pool pool = new Pool("pool-test");
        PoolHandler poolHandler = new PoolHandler(pool);
        
        OptionMap optionMap = poolHandler.getUndertowOptionMap(env, "UNDERTOW_OPTIONS_");
        
        Assert.assertEquals(optionMap.get(Options.KEEP_ALIVE, false), true);
        Assert.assertEquals(optionMap.get(Options.BALANCING_CONNECTIONS, 0), 100);
    }
}
    