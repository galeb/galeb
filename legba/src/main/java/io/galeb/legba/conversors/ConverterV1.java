package io.galeb.legba.conversors;

import io.galeb.core.entity.VirtualHost;

import java.util.List;

public class ConverterV1 implements Converter {

    public static final String API_VERSION = "v1";

    @Override
    public String convertToString(List<VirtualHost> virtualHostList) {
        return "Length: " + virtualHostList.size();
    }
}
