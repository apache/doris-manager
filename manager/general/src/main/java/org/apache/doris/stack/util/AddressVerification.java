package org.apache.doris.stack.util;

import sun.net.util.IPAddressUtil;

public class AddressVerification {

    public static  boolean IpVerification(String ipStr) {
        boolean iPv4LiteralAddress = IPAddressUtil.isIPv4LiteralAddress(ipStr);
        boolean iPv6LiteralAddress = IPAddressUtil.isIPv6LiteralAddress(ipStr);
        if (!(iPv4LiteralAddress || iPv6LiteralAddress)) {
            return false;
        }
        return true;
    }
}
