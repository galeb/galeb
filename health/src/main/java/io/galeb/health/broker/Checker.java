package io.galeb.health.broker;

import io.galeb.health.SystemEnvs;
import io.galeb.health.externaldata.ManagerClient;
import io.galeb.manager.entity.Target;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.galeb.health.broker.Checker.State.FAIL;
import static io.galeb.health.broker.Checker.State.OK;
import static io.galeb.health.broker.Checker.State.UNKNOWN;
import static io.galeb.health.externaldata.ManagerClient.*;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

@Component
public class Checker {

    public enum State {
        OK,
        FAIL,
        UNKNOWN
    }

    public static final AtomicLong LAST_CALL = new AtomicLong(0L);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AsyncHttpClient asyncHttpClient;
    private final ManagerClient managerClient;

    @Autowired
    public Checker(final ManagerClient managerClient) {
        this.asyncHttpClient = asyncHttpClient(config()
                .setFollowRedirect(false)
                .setSoReuseAddress(true)
                .setKeepAlive(false)
                .setConnectTimeout(Integer.parseInt(SystemEnvs.TEST_TIMEOUT.getValue()))
                .setPooledConnectionIdleTimeout(1)
                .setMaxConnectionsPerHost(1).build());
        this.managerClient = managerClient;
    }

    @SuppressWarnings("unused")
    @JmsListener(destination = "galeb-health", concurrency = "5-5", containerFactory = "containerFactory")
    public void check(Target target) throws ExecutionException, InterruptedException {
        LAST_CALL.set(System.currentTimeMillis());
        final Map<String, String> properties = target.getProperties();
        final AtomicReference<String> hcPath = new AtomicReference<>(properties.get(PROP_HEALTHCHECK_PATH));
        hcPath.compareAndSet(null,"/");
        final String hcStatusCode = properties.get(PROP_HEALTHCHECK_CODE);
        final String hcBody = properties.get(PROP_HEALTHCHECK_RETURN);
        final String hcHost = properties.getOrDefault(ManagerClient.PROP_HEALTHCHECK_HOST, target.getName());
        final String lastReason = properties.get(ManagerClient.PROP_STATUS_DETAILED);
        long start = System.currentTimeMillis();

        RequestBuilder requestBuilder = new RequestBuilder("GET").setUrl(target.getName() + hcPath.get()).setVirtualHost(hcHost);
        asyncHttpClient.executeRequest(requestBuilder, new AsyncCompletionHandler<Response>() {
            @Override
            public Response onCompleted(Response response) throws Exception {
                if (checkFailStatusCode(response)) return response;
                if (checkFailBody(response)) return response;
                definePropertiesAndUpdate(OK, OK.toString());
                return response;
            }

            @Override
            public void onThrowable(Throwable t) {
                definePropertiesAndUpdate(UNKNOWN, t.getMessage());
            }

            private boolean checkFailBody(Response response) {
                if (hcBody != null) {
                    String body = response.getResponseBody();
                    if (body != null && !body.isEmpty() && !body.contains(hcBody)) {
                        definePropertiesAndUpdate(FAIL, "Body FAIL");
                        return true;
                    }
                }
                return false;
            }

            private boolean checkFailStatusCode(Response response) {
                if (hcStatusCode != null) {
                    int statusCode = response.getStatusCode();
                    if (statusCode != Integer.parseInt(hcStatusCode)) {
                        definePropertiesAndUpdate(FAIL, "Status code FAIL");
                        return true;
                    }
                }
                return false;
            }

            private void definePropertiesAndUpdate(Checker.State state, String reason) {
                String newHealthyState = state.toString();

                target.getProperties().put(ManagerClient.PROP_HEALTHY, newHealthyState);
                target.getProperties().put(ManagerClient.PROP_STATUS_DETAILED, reason);
                String scheduleId = target.getProperties().get("SCHEDULER_ID");
                String logMessage = "[" + scheduleId + "] " + target.getName() + hcPath.get() + ": " + reason
                        + " (request time: " + (System.currentTimeMillis() - start) + " ms)";
                if (state.equals(OK)) {
                    logger.info(logMessage);
                } else {
                    logger.warn(logMessage);
                }
                if (lastReason == null || !reason.equals(lastReason)) {
                    try {
                        managerClient.update(target);
                    } catch (ExecutionException | InterruptedException e) {
                        logger.error(ExceptionUtils.getStackTrace(e));
                    }
                }
            }

        });
    }

}
