package de.tum.in.net.group17.onion.config;

import java.net.InetAddress;
import java.time.Duration;

/**
 * Interface for the ConfigurationProvider, supplying all values for the Onion interfaces operation that can be
 * configured externally.
 * Created by Christoph Rudolf on 09.07.17.
 */
public interface ConfigurationProvider {
    /**
     * Get the host ID of this peer.
     * (Hash of the public key specified in the configuration)
     *
     * @return The ID of this host.
     */
    String getHostId();


    /**
     * Get the 'intermediate_hops' parameter from the configuration.
     *
     * @return The number of intermediate hops to use.
     */
    int getIntermediateHopCount();


    /**
     * Get the parsed 'round_interval' parameter from the configuration.
     *
     * @return The round interval in seconds.
     */
    Duration getRoundInterval();


    /**
     * Get the IP address contained in the Onion 'listen_address' parameter in the configuration.
     *
     * @return The Onion P2P address of this host.
     */
    InetAddress getOnionP2PHost();

    /**
     * Get the port contained in the Onion 'listen_address' parameter in the configuration.
     *
     * @return The Onion P2P port of this host.
     */
    int getOnionP2PPort();


    /**
     * Get the IP address contained in the Onion 'api_address' parameter in the configuration.
     *
     * @return The Onion API address of this host.
     */
    InetAddress getOnionApiHost();

    /**
     * Get the port contained in the Onion 'api_address' parameter in the configuration.
     *
     * @return The Onion API port of this host.
     */
    int getOnionApiPort();


    /**
     * Get the IP address contained in the Onion Auth 'api_address' parameter in the configuration.
     *
     * @return The API address of the Onion Auth module to use.
     */
    InetAddress getAuthApiHost();

    /**
     * Get the port contained in the Onion Auth 'api_address' parameter in the configuration.
     *
     * @return The API port of the Onion Auth module to use.
     */
    int getAuthApiPort();


    /**
     * Get the IP address contained in the RPS 'api_address' parameter in the configuration.
     *
     * @return The API address of the RPS module to use.
     */
    InetAddress getRpsApiHost();

    /**
     * Get the API port contained in the RPS 'api_address' parameter in the configuration.
     *
     * @return The API port of the RPS module to use.
     */
    int getRpsApiPort();
}
