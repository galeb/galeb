package io.galeb.router.services;

import io.galeb.router.SystemEnvs;
import io.galeb.router.cluster.ExternalData;

import java.util.List;

public interface ExternalDataService {
    String ROOT_KEY         = "/";
    String PREFIX_KEY       = ROOT_KEY + SystemEnvs.CLUSTER_ID.getValue();
    String VIRTUALHOSTS_KEY = PREFIX_KEY + "/virtualhosts";
    String POOLS_KEY        = PREFIX_KEY + "/pools";

    List<ExternalData> listFrom(String key);

    List<ExternalData> listFrom(String key, boolean recursive);

    List<ExternalData> listFrom(ExternalData node);

    ExternalData node(String key);

    ExternalData node(String key, boolean recursive);

    ExternalData node(String key, ExternalData.Generic def);

    ExternalData node(String key, boolean recursive, ExternalData.Generic def);

    boolean exist(String key);
}
