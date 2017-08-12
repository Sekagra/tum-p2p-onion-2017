package de.tum.in.net.group17.onion.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

/**
 * Created by Marko Dorfhuber(PraMiD) on 01.08.17.
 */
public class ConfigurationProviderMock implements ConfigurationProvider {

    private int onionApiPort;
    private int onionPort;
    private int authModulePort;
    private int rpsModulePort;

    private int intermediateHopCount;
    private Duration roundInterval;

    private InetAddress rpsModuleAddress;
    private InetAddress authModuleAddress;
    private InetAddress onionModuleAddress;
    private InetAddress onionListenAddress;

    public ConfigurationProviderMock(int onionApiPort, int onionPort, int authModulePort, int rpsModulePort,
                                     int intermediateHopCount, String rpsModuleAddress, String authModuleAddress,
                                     String onionModuleAddress, String onionListenAddress,
                                     long roundInterval) throws UnknownHostException {
        this.onionApiPort = onionApiPort;
        this.onionPort = onionPort;
        this.authModulePort = authModulePort;
        this.rpsModulePort = rpsModulePort;
        this.rpsModuleAddress = InetAddress.getByName(rpsModuleAddress);
        this.authModuleAddress = InetAddress.getByName(authModuleAddress);
        this.onionModuleAddress = InetAddress.getByName(onionModuleAddress);
        this.onionListenAddress = InetAddress.getByName(onionListenAddress);

        this.intermediateHopCount = intermediateHopCount;
        this.roundInterval = Duration.ofSeconds(roundInterval);
    }

    @Override
    public int getOnionApiPort() {
        return onionApiPort;
    }

    @Override
    public int getOnionP2PPort() {
        return onionPort;
    }

    @Override
    public InetAddress getOnionApiHost() {
        return onionModuleAddress;
    }

    @Override
    public int getAuthApiPort() {
        return authModulePort;
    }

    @Override
    public InetAddress getAuthApiHost() {
        return authModuleAddress;
    }

    @Override
    public int getIntermediateHopCount() {
        return intermediateHopCount;
    }

    @Override
    public Duration getRoundInterval() {
        return roundInterval;
    }

    @Override
    public InetAddress getOnionP2PHost() {
        return onionListenAddress;
    }

    @Override
    public InetAddress getRpsApiHost() {
        return rpsModuleAddress;
    }

    @Override
    public int getRpsApiPort() {
        return rpsModulePort;
    }
}
