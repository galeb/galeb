package io.galeb.legba.conversors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

public class ConverterBuilder {

    private static Map<String, Converter> mapConverter = new HashMap() {{
        put(ConverterV1.API_VERSION, new ConverterV1());
        put(ConverterV2.API_VERSION, new ConverterV2());
    }};

    public static Converter getConversor(String apiVersion) throws ConverterNotFoundException {
        String api = ConverterV1.API_VERSION;
        if (apiVersion != null) {
            api = apiVersion;
        }
        Converter conv = mapConverter.get(api);
        if (conv == null) {
            throw new ConverterNotFoundException();
        }
        return conv;
    }

    @ResponseStatus(value= HttpStatus.BAD_REQUEST, reason = "Converter not found")
    private static class ConverterNotFoundException extends Exception {

    }

}
