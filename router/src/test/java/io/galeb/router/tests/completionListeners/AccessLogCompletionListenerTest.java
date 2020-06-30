package io.galeb.router.tests.completionListeners;

import com.google.gson.JsonObject;
import io.galeb.router.handlers.completionListeners.AccessLogCompletionListener;
import io.undertow.server.Connectors;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mockito;

import java.net.InetSocketAddress;

public class AccessLogCompletionListenerTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void getJsonObjectTest() {
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

        JsonObject jsonObject = accessLogCompletionListener.getJsonObject(httpServerExchange);

        Assert.assertEquals("1", jsonObject.getAsJsonPrimitive("@version").getAsString());
        Assert.assertEquals("hostname.localenv", jsonObject.getAsJsonPrimitive("host").getAsString());
        Assert.assertEquals(AccessLogCompletionListener.SHORT_MESSAGE, jsonObject.getAsJsonPrimitive("short_message").getAsString());
        Assert.assertEquals("vhost.host.virtual", jsonObject.getAsJsonPrimitive("vhost").getAsString());
        Assert.assertEquals("GALEB,OTHER,ACCESS", jsonObject.getAsJsonPrimitive("_tags").getAsString());
        Assert.assertEquals("1.2.3.4", jsonObject.getAsJsonPrimitive("remote_addr").getAsString());
        Assert.assertEquals("GET", jsonObject.getAsJsonPrimitive("request_method").getAsString());
        Assert.assertEquals("/test", jsonObject.getAsJsonPrimitive("request_uri").getAsString());
        Assert.assertEquals("HTTP", jsonObject.getAsJsonPrimitive("server_protocol").getAsString());
        Assert.assertEquals("-", jsonObject.getAsJsonPrimitive("http_referer").getAsString());
        Assert.assertEquals("-", jsonObject.getAsJsonPrimitive("http_x_mobile_group").getAsString());
        Assert.assertEquals("600", jsonObject.getAsJsonPrimitive("status").getAsString());
        Assert.assertNotNull(jsonObject.getAsJsonPrimitive("body_bytes_sent").getAsString());
        Assert.assertNotNull(jsonObject.getAsJsonPrimitive("request_time").getAsString());
        Assert.assertEquals("UNKNOWN_TARGET", jsonObject.getAsJsonPrimitive("upstream_addr").getAsString());
        Assert.assertEquals("200", jsonObject.getAsJsonPrimitive("upstream_status").getAsString());
        Assert.assertEquals("-", jsonObject.getAsJsonPrimitive("upstream_response_length").getAsString());
        Assert.assertEquals("-", jsonObject.getAsJsonPrimitive("http_user_agent").getAsString());
        Assert.assertEquals("-", jsonObject.getAsJsonPrimitive("request_id_final").getAsString());
        Assert.assertEquals("-", jsonObject.getAsJsonPrimitive("http_x_forwarded_for").getAsString());
    }

    private HeaderMap getRequestHeaders() {
        HeaderMap headerMap = new HeaderMap();
        headerMap.add(HttpString.tryFromString("HOST"),"vhost.host.virtual");
        return headerMap;
    }
}


