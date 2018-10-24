package io.galeb.health.util;

import io.galeb.core.log.JsonEventToLogger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class LocalIP {

    public static String encode() {
        final List<String> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface localInterface = ifs.nextElement();
                if (!localInterface.isLoopback() && localInterface.isUp()) {
                    Enumeration<InetAddress> ips = localInterface.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        InetAddress ipaddress = ips.nextElement();
                        if (ipaddress instanceof Inet4Address) {
                            ipList.add(ipaddress.getHostAddress());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            JsonEventToLogger eventToLogger = new JsonEventToLogger(LocalIP.class);
            eventToLogger.sendError(e);
        }
        String ip = String.join("-", ipList);
        if ("".equals(ip)) {
            ip = "undef-" + System.currentTimeMillis();
        }
        return ip.replaceAll("[:%]", "");
    }
}
