package io.galeb.core.so;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("Duplicates")
public class LocalIP {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalIP.class);

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
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        String ip = String.join("-", ipList);
        if ("".equals(ip)) {
            ip = "undef-" + System.currentTimeMillis();
        }
        return ip.replaceAll("[:%]", "");
    }
}
