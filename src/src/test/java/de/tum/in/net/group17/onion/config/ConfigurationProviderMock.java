package de.tum.in.net.group17.onion.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Marko Dorfhuber(PraMiD) on 01.08.17.
 */
public class ConfigurationProviderMock implements ConfigurationProvider {

    private int onionApiPort;
    private int onionPort;
    private int authModulePort;
    private int rpsModulePort;

    private int intermediateHopCount;

    private InetAddress rpsModuleAddress;
    private InetAddress authModuleAddress;

    public ConfigurationProviderMock(int onionApiPort, int onionPort, int authModulePort, int rpsModulePort, int intermediateHopCount, String rpsModuleAddress, String authModuleAddress) throws UnknownHostException {
        this.onionApiPort = onionApiPort;
        this.onionPort = onionPort;
        this.authModulePort = authModulePort;
        this.intermediateHopCount = intermediateHopCount;
        this.rpsModulePort = rpsModulePort;
        this.rpsModuleAddress = InetAddress.getByName(rpsModuleAddress);
        this.authModuleAddress = InetAddress.getByName(authModuleAddress);
    }

    @Override
    public int getOnionApiPort() {
        return onionApiPort;
    }

    @Override
    public int getOnionPort() {
        return onionPort;
    }

    @Override
    public int getAuthModulePort() {
        return authModulePort;
    }

    @Override
    public InetAddress getAuthModuleHost() {
        return authModuleAddress;
    }

    @Override
    public int getIntermediateHopCount() {
        return intermediateHopCount;
    }

    @Override
    public InetAddress getRpsModuleHost() {
        return rpsModuleAddress;
    }

    @Override
    public int getRpsModulePort() {
        return rpsModulePort;
    }
}
