package io.galeb.router.tests.completionListeners;

import java.net.InetSocketAddress;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mockito;

import io.galeb.router.handlers.completionListeners.AccessLogCompletionListener;
import io.undertow.server.Connectors;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

public class AccessLogCompletionListenerTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void getJsonObjectTest() throws JSONException {
        environmentVariables.set("HOSTNAME", "hostname.localenv");
        environmentVariables.set("LOGGING_TAGS", "GALEB,OTHER");

        AccessLogCompletionListener accessLogCompletionListener = new AccessLogCompletionListener();

        HttpServerExchange httpServerExchange = new HttpServerExchange(Mockito.mock(ServerConnection.class), getRequestHeaders(), null, 0);
        httpServerExchange.setSourceAddress(new InetSocketAddress("1.2.3.4", 44444));
        httpServerExchange.setRequestMethod(HttpString.tryFromString("GET"));
        httpServerExchange.setRequestURI("/test");
        httpServerExchange.setProtocol(HttpString.tryFromString("HTTP"));
        httpServerExchange.setStatusCode(200);
        Connectors.setRequestStartTime(httpServerExchange);

        JSONObject jsonObject = new JSONObject(accessLogCompletionListener.getJsonObject(httpServerExchange));

        Assert.assertEquals("1", jsonObject.getString("@version"));
        Assert.assertEquals("hostname.localenv", jsonObject.getString("host"));
        Assert.assertEquals(AccessLogCompletionListener.SHORT_MESSAGE, jsonObject.getString("short_message"));
        Assert.assertEquals("vhost.host.virtual", jsonObject.getString("vhost"));
        Assert.assertEquals("GALEB,OTHER,ACCESS", jsonObject.getString("_tags"));
        Assert.assertEquals("1.2.3.4", jsonObject.getString("remote_addr"));
        Assert.assertEquals("GET", jsonObject.getString("request_method"));
        Assert.assertEquals("/test", jsonObject.getString("request_uri"));
        Assert.assertEquals("HTTP", jsonObject.getString("server_protocol"));
        Assert.assertEquals("-", jsonObject.getString("http_referer"));
        Assert.assertEquals("-", jsonObject.getString("http_x_mobile_group"));
        Assert.assertEquals("600", jsonObject.getString("status"));
        Assert.assertNotNull(jsonObject.getString("body_bytes_sent"));
        Assert.assertNotNull(jsonObject.getString("request_time"));
        Assert.assertEquals("UNKNOWN_TARGET", jsonObject.getString("upstream_addr"));
        Assert.assertEquals("200", jsonObject.getString("upstream_status"));
        Assert.assertEquals("-", jsonObject.getString("upstream_response_length"));
        Assert.assertEquals("-", jsonObject.getString("http_user_agent"));
        Assert.assertEquals("-", jsonObject.getString("request_id_final"));
        Assert.assertEquals("-", jsonObject.getString("http_x_forwarded_for"));
    }

    private HeaderMap getRequestHeaders() {
        HeaderMap headerMap = new HeaderMap();
        headerMap.add(HttpString.tryFromString("HOST"),"vhost.host.virtual");
        return headerMap;
    }
}


