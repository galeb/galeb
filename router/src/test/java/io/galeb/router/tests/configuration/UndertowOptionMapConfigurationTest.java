package io.galeb.router.tests.configuration;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.xnio.OptionMap;
import org.xnio.Options;

import io.galeb.router.configurations.UndertowOptionMapConfiguration;

public class UndertowOptionMapConfigurationTest {
    
    @Test
    public void testBuildUndertowOptionMapFromEnvironmentWithOptionKeepAlive() throws Exception {
        
        HashMap<String, String> env = new HashMap<String, String>();
        env.put("UNDERTOW_OPTIONS_KEEP_ALIVE", "true");
        
        UndertowOptionMapConfiguration undertowOptionMapConfiguration = new UndertowOptionMapConfiguration();
        OptionMap optionMap = undertowOptionMapConfiguration.buildUndertowOptionMapFromEnvironment("UNDERTOW_OPTIONS_", env);
        
        Assert.assertEquals(optionMap.get(Options.KEEP_ALIVE, false), true);
    }
    
    @Test
    public void testGetUndertowOptionMapWithTwoBooleanOptions() throws Exception {
        HashMap<String, String> env = new HashMap<String, String>();
        env.put("UNDERTOW_OPTIONS_KEEP_ALIVE", "true");
        env.put("UNDERTOW_OPTIONS_ALLOW_BLOCKING", "true");
        
        UndertowOptionMapConfiguration undertowOptionMapConfiguration = new UndertowOptionMapConfiguration();
        OptionMap optionMap = undertowOptionMapConfiguration.buildUndertowOptionMapFromEnvironment("UNDERTOW_OPTIONS_", env);
        
        Assert.assertEquals(optionMap.get(Options.KEEP_ALIVE, false), true);
        Assert.assertEquals(optionMap.get(Options.ALLOW_BLOCKING, false), true);
    }
    
    @Test
    public void testGetUndertowOptionMapWithBooleanOptionAndIntegerOption() throws Exception {
        HashMap<String, String> env = new HashMap<String, String>();
        env.put("UNDERTOW_OPTIONS_KEEP_ALIVE", "true");
        env.put("UNDERTOW_OPTIONS_BALANCING_CONNECTIONS", "100");
        
        UndertowOptionMapConfiguration undertowOptionMapConfiguration = new UndertowOptionMapConfiguration();
        OptionMap optionMap = undertowOptionMapConfiguration.buildUndertowOptionMapFromEnvironment("UNDERTOW_OPTIONS_", env);
        
        Assert.assertEquals(optionMap.get(Options.KEEP_ALIVE, false), true);
        Assert.assertEquals(optionMap.get(Options.BALANCING_CONNECTIONS, 0), 100);
    }
}
    