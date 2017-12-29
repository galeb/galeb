package io.galeb.legba.conversors;

import io.galeb.core.entity.VirtualHost;

import java.util.List;

public interface Converter {

    String convertToString(List<VirtualHost> virtualHostList, String numRouters, String version);

}
