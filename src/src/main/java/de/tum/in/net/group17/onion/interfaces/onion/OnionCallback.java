package de.tum.in.net.group17.onion.interfaces.onion;

import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Implements callback methods to notify a superordinate module of changes
 * that are relevant to other modules.
 * Created by Christoph Rudolf on 17.07.17.
 */
public interface OnionCallback {
    /**
     * Notify the superordinate module of an error that has occurred while serving a request from the CM module.
     *
     * @param tunnelId The ID of the tunnel on which the error has happened. This might be zero if no tunnel was able to
     *                 be established.
     * @param type The type of the request that resulted in an error.
     */
    void error(int tunnelId, MessageType type);

    /**
     * Notify the superordinate module of incoming voice data to this peer.
     *
     * @param tunnelId The ID of the tunnel that received data.
     * @param data The data received for this tunnel.
     */
    void tunnelData(int tunnelId, byte[] data);

    /**
     * Notify the superordinate module of an incoming new tunnel.
     *
     * @param segment A reference to the TunnelSegment that matches in the incoming tunnel.
     */
    void tunnelIncoming(TunnelSegment segment);

    /**
     * Notify the superordinate module of a successful tunnel teardown.
     * @param tunnelId The ID of the tunnel that has been teared down.
     */
    void tunnelDestroyed(int tunnelId);
}
