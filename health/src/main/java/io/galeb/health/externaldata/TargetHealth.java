package io.galeb.health.externaldata;

import io.galeb.health.SystemEnvs;
import io.galeb.manager.entity.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class TargetHealth {

    public enum HcState {
        FAIL,
        UNKNOWN,
        OK
    }

    public static final String PROP_HEALTHCHECK_RETURN = "hcBody";
    public static final String PROP_HEALTHCHECK_PATH   = "hcPath";
    public static final String PROP_HEALTHCHECK_HOST   = "hcHost";
    public static final String PROP_HEALTHCHECK_CODE   = "hcStatusCode";
    public static final String PROP_HEALTHY            = "healthy";
    public static final String PROP_STATUS_DETAILED    = "status_detailed";

    private final String managerUrl = SystemEnvs.MANAGER_URL.getValue();

    private final ManagerClient managerClient;

    @Autowired
    public TargetHealth(final ManagerClient managerClient) {
        this.managerClient = managerClient;
    }

    public void patchTarget(Target target) {
        String targetUrl = managerUrl + "/target/" + target.getId();
        String healthy = target.getProperties().get(PROP_HEALTHY);
        String statusDetailed = target.getProperties().get(PROP_STATUS_DETAILED);
        healthy = healthy != null ? healthy : HcState.UNKNOWN.toString();
        statusDetailed = statusDetailed != null ? statusDetailed : HcState.UNKNOWN.toString();
        managerClient.renewToken();
        String body = "{\"properties\": { \"" + PROP_HEALTHY + "\":\"" + healthy + "\",\"" + PROP_STATUS_DETAILED + "\":\"" + statusDetailed + "\" }}";
        managerClient.patch(targetUrl, body);
    }

    private Map<String, String> hcPropsMap(String hcPath, String hcStatusCode, String hcBody, String hcHost) {
        final Map<String, String> properties = new HashMap<>();
        properties.put(PROP_HEALTHCHECK_PATH, hcPath);
        properties.put(PROP_HEALTHCHECK_HOST, hcHost);
        properties.put(PROP_HEALTHCHECK_CODE, hcStatusCode);
        properties.put(PROP_HEALTHCHECK_RETURN, hcBody);
        return properties;
    }

    public Stream<Target> targetsByEnvName(String environmentName) {
        return managerClient.targetsByEnvName(environmentName).map(target -> {
                String hcPath = target.getParent().getProperties().get(PROP_HEALTHCHECK_PATH);
                String hcStatusCode = target.getParent().getProperties().get(PROP_HEALTHCHECK_CODE);
                String hcBody = target.getParent().getProperties().get(PROP_HEALTHCHECK_RETURN);
                String hcHost = target.getParent().getProperties().get(PROP_HEALTHCHECK_HOST);
                final Map<String, String> properties = hcPropsMap(hcPath, hcStatusCode, hcBody, hcHost);
                target.getProperties().putAll(properties);
                return target;
        });
    }
}
