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


    /**
     * Create a new ConfigurationProvider providing the specified parameters.
     * This configuration provider should only be used for testing purposes.
     *
     *
     * @param onionApiPort The port of the Onion API.
     * @param onionPort The Onion P2P port.
     * @param authModulePort The port of the Onion Auth API.
     * @param rpsModulePort The port of the RPS API.
     * @param intermediateHopCount Number of intermediate hops to use in a tunnel.
     * @param rpsModuleAddress The address of the RPS API.
     * @param authModuleAddress The address of the Onion Auth API.
     * @param onionModuleAddress The address of the Onion API.
     * @param onionListenAddress The Onion P2P address.
     * @param roundInterval The round interval in seconds.
     *
     * @throws UnknownHostException If an unknown address was provided as parameter.
     */
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

    /**
     * @inheritDoc
     */
    @Override
    public String getHostId() {
        return "";
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getOnionApiPort() {
        return onionApiPort;
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getOnionP2PPort() {
        return onionPort;
    }

    /**
     * @inheritDoc
     */
    @Override
    public InetAddress getOnionApiHost() {
        return onionModuleAddress;
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getAuthApiPort() {
        return authModulePort;
    }

    /**
     * @inheritDoc
     */
    @Override
    public InetAddress getAuthApiHost() {
        return authModuleAddress;
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getIntermediateHopCount() {
        return intermediateHopCount;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Duration getRoundInterval() {
        return roundInterval;
    }

    /**
     * @inheritDoc
     */
    @Override
    public InetAddress getOnionP2PHost() {
        return onionListenAddress;
    }

    /**
     * @inheritDoc
     */
    @Override
    public InetAddress getRpsApiHost() {
        return rpsModuleAddress;
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getRpsApiPort() {
        return rpsModulePort;
    }
}
