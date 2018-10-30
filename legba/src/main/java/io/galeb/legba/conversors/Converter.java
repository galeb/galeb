package io.galeb.legba.conversors;

public interface Converter {

    String convertToString(String logCorrelation, String version, String networkId, Long envId, String groupId);

    String getApiVersion();
}
