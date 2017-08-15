package de.tum.in.net.group17.onion.interfaces.onion;

import de.tum.in.net.group17.onion.interfaces.authentication.AuthException;
import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.parser.onionapi.OnionCoverParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;

import java.util.List;
import java.util.Map;

/**
 * This interface is responsible for serving incoming requests of fellow Onion modules and sending requests to them.
 * It is responsible for all Onion to Onion communication.
 * Created by Christoph Rudolf on 21.06.17.
 */
public interface OnionInterface {
    /**
     * Start listening for incoming Onion UDP packets.
     *
     * @param callback Callback for the caller to be notified of certain events that affect other modules.
     */
    void listen(OnionCallback callback);

    /**
     * Set the list of tunnels this peer has created or received
     *
     * @param started The list of tunnels this peer created.
     * @param incoming The list of tunnels this peer is an endpoint on.
     */
    void setTunnels(Map<Integer, Tunnel> started, Map<Integer, Tunnel> incoming);

    /**
     * Set the map with tunnel segments for all cases in which this peer is an intermediate hop.
     *
     * @param segments The segment map to work on.
     */
    void setSegments(Map<Lid, TunnelSegment> segments);

    /**
     * Extend the given tunnel by contacting the new peer and adding a segment to the tunnel in case of success.
     *
     *
     * @param tunnel The tunnel to advance.
     * @param peer The peer to make part of the tunnel at the current end.
     *
     * @throws OnionException On any error during tunnel extension. Use OnionException.getMessage for further information.
     * @throws InterruptedException If we were interrupted while waiting for Onion P2P response.
     */
    void extendTunnel(Tunnel tunnel, Peer peer) throws OnionException, InterruptedException;

    /**
     * Orders the onion module to destroy a tunnel with a given ID.
     *
     *
     * @param tunnelId The ID of the tunnel to destroy.
     *
     * @throws OnionException On any error during tunnel extension. Use OnionException.getMessage for further information.
     */
    void destroyTunnelById(int tunnelId) throws OnionException;

    /**
     * Instructs the onion module to send cover traffic.
     *
     *
     * @param msg The OnionCoverParsedMessage with all necessary data regarding the cover traffic to sent.
     *
     * @throws OnionException On any error during tunnel extension. Use OnionException.getMessage for further information.
     */
    void sendVoiceData(OnionTunnelDataParsedMessage msg) throws OnionException;

    /**
     * Instructs the onion module to send cover traffic.
     *
     *
     * @param msg The OnionCoverParsedMessage with all necessary data regarding the cover traffic to sent.
     *
     * @throws OnionException On any error during tunnel extension. Use OnionException.getMessage for further information.
     */
    void sendCoverData(OnionCoverParsedMessage msg) throws OnionException;

    /**
     * Send the established message type over the given tunnel.
     *
     * @param tunnel The tunnel to send the established message over.
     *
     * @throws OnionException On any error during tunnel extension. Use OnionException.getMessage for further information.
     */
    void sendEstablished(Tunnel tunnel) throws OnionException;

    /**
     * Send the established message type with two local identifiers that indicates the refresh of a previous established
     * tunnel. This is sent through the new tunnel.
     *
     * @param newTunnel The new tunnel that will be used for ongoing communication.
     * @param oldTunnel The tunnel that has been previously used for communication.
     *
     * @throws OnionException On any error during tunnel extension. Use OnionException.getMessage for further information.
     */
    void sendEstablished(Tunnel newTunnel, Tunnel oldTunnel) throws OnionException;
}
