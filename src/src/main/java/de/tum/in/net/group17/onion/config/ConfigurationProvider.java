package de.tum.in.net.group17.onion.config;

import java.net.InetAddress;
import java.time.Duration;

/**
 * Interface for the ConfigurationProvider, supplying all values for the Onion interfaces operation that can be
 * configured externally.
 * Created by Christoph Rudolf on 09.07.17.
 */
public interface ConfigurationProvider {
    int getIntermediateHopCount();
    Duration getRoundInterval();

    InetAddress getOnionP2PHost();
    int getOnionP2PPort();

    InetAddress getOnionApiHost();
    int getOnionApiPort();

    InetAddress getAuthApiHost();
    int getAuthApiPort();

    InetAddress getRpsApiHost();
    int getRpsApiPort();
}
