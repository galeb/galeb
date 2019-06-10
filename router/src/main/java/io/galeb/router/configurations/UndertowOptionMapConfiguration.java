package io.galeb.router.configurations;

import java.util.Properties;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.xnio.OptionMap;
import org.xnio.Options;

import io.galeb.core.enums.SystemEnv;

@Configuration
public class UndertowOptionMapConfiguration {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Bean("undertowOptionMap")
    @Scope("singleton")
    public OptionMap getUndertowOptionMap() {
        String prefix = SystemEnv.PREFIX_UNDERTOW_CLIENT_OPTION.getValue();
        Map<String, String> environmentVariables = System.getenv();
        
        OptionMap optionMap = buildUndertowOptionMapFromEnvironment(prefix, environmentVariables);
        
        logger.info("Client undertow optionMap: " + optionMap.toString());
        
        return optionMap;
    }

    public OptionMap buildUndertowOptionMapFromEnvironment(String prefix, Map<String, String> environmentVariables) {
        if (!prefix.endsWith("_")) {
            prefix = prefix + "_";
        }
        
        String prefixWithoutUnderscoreAtEnd = prefix.substring(0, prefix.length() - 1); 
        Properties properties = new Properties();
        for (Entry<String, String> entry : environmentVariables.entrySet()) {
            String key = entry.getKey();
            
            if (key.startsWith(prefix)) {
                String keyWithoutPrefix = key.substring(prefix.length());
                String fieldValue = entry.getValue();
            
                String className = Options.class.getName();
                String propertyKey = prefixWithoutUnderscoreAtEnd + "." + className + "." + keyWithoutPrefix;
                properties.put(propertyKey, fieldValue);
            }
        }

        OptionMap optionMap = OptionMap.builder().parseAll(properties, prefixWithoutUnderscoreAtEnd).getMap();
        return optionMap;
    }
    
}
