package de.tum.in.net.group17.onion.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A ConfigurationProvider is in charge of parsing and reading configurations files to supply the relevant values to
 * certain modules (e.g. ports for API connections)
 * Created by Christoph Rudolf on 27.05.17.
 */
public class ConfigurationProvider {
    public int getOnionApiPort() {
        return 9002;
    }

    public int getOnionPort() {
        return 9003;
    }

    public int getAuthModuleRequestPort() {
        return 9001;
    }

    public InetAddress getAuthModuleHost() {
        try {
            return InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
