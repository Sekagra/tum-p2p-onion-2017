package de.tum.in.net.group17.onion.config;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A ConfigurationProviderImpl is in charge of parsing and reading configurations files to supply the relevant values to
 * certain modules (e.g. ports for API connections)
 * Created by Christoph Rudolf on 27.05.17.
 */
public class ConfigurationProviderImpl implements ConfigurationProvider {
    private Logger logger;

    public ConfigurationProviderImpl() {
        this.logger = Logger.getLogger(ConfigurationProvider.class);
    }

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

    @Override
    public int getIntermediateHopCount() {
        return 2;
    }
}
