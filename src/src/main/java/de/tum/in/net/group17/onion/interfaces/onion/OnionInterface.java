package de.tum.in.net.group17.onion.interfaces.onion;

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
     * Set the list of tunnels this peer has created.
     * @param tunnel The tunnel list to work on.
     */
    void setTunnel(List<Tunnel> tunnel);

    /**
     * Set the map with tunnel segments for all cases in which this peer is an intermediate hop.
     * @param segments The segment map to work on.
     */
    void setSegments(Map<Lid, TunnelSegment> segments);

    /**
     * Extend the given tunnel by contacting the new peer and adding a segment to the tunnel in case of success.
     * @param tunnel The tunnel to advance.
     * @param peer The peer to make part of the tunnel at the current end.
     */
    void extendTunnel(Tunnel tunnel, Peer peer) throws OnionException, InterruptedException;

    /**
     * Orders the onion module to destroy a tunnel.
     *
     * @param tunnelId The ID of the tunnel to destroy.
     */
    void destroyTunnel(int tunnelId);

    /**
     * Instructs the onion module to send cover traffic.
     * @param msg The OnionCoverParsedMessage with all necessary data regarding the cover traffic to sent.
     */
    void sendVoiceData(OnionTunnelDataParsedMessage msg);

    /**
     * Instructs the onion module to send cover traffic.
     * @param msg The OnionCoverParsedMessage with all necessary data regarding the cover traffic to sent.
     */
    void sendCoverData(OnionCoverParsedMessage msg);
}
