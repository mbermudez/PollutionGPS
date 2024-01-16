package com.example.pollutiongps.gpsip

import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale


fun getIPAddress(useIPv4: Boolean): String {
    try {
        val interfaces: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            val addrs = Collections.list(intf.inetAddresses)
            for (addr in addrs) {
                if (!addr.isLoopbackAddress) {
                    val sAddr = addr.hostAddress
                    val isIPv4 = sAddr.indexOf(':') < 0
                    if (useIPv4) {
                        if (isIPv4) return sAddr
                    } else {
                        if (!isIPv4) {
                            val delim = sAddr.indexOf('%')
                            return if (delim < 0) sAddr.toUpperCase(Locale.ROOT) else sAddr.substring(
                                0,
                                delim
                            ).toUpperCase(Locale.ROOT)
                        }
                    }
                }
            }
        }
    } catch (ignored: Exception) {
    }
    return ""
}