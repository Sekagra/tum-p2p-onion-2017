package de.tum.in.net.group17.onion.interfaces.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.onionapi.*;

/**
 * This interface is responsible for serving incoming requests of the UI/CM thus essentially providing the Onion API
 * given in the specification of the application.
 *
 * Created by Christoph Rudolf on 11.06.17.
 */
public interface OnionApiInterface {
    /**
     * Start listening for connections on this interface.
     *
     * @param callback Callbacks used to inform about incoming messages.
     */
    void listen(OnionApiCallback callback);

    /**
     * Inform the client (The last one that connected to the API) that a new tunnel was established to out peer.
     *
     * @param tunnelId The ID of the new tunnel that is incoming to us.
     *
     * @throws OnionApiException If the channel is in an invalid state.
     */
    void sendIncoming(int tunnelId) throws OnionApiException;

    /**
     * Inform the client that a given tunnel is established.
     *
     * @param tunnelId The ID of the new tunnel that is incoming to us.
     * @param key The peer's hostkey as it is requested by ONION TUNNEL READY.
     * @throws OnionApiException If the channel is in an invalid state.
     */
    void sendReady(int tunnelId, byte[] key) throws OnionApiException;

    /**
     * Send a error message to one of our clients.
     *
     * @param tunnelId The ID of the tunnel on which the error has happened. This might be zero if no tunnel was able to
     *                 be established.
     * @param type The type of the request that resulted in an error.
     *
     * @throws OnionApiException If the channel is in an invalid state.
     */
    void sendError(int tunnelId, MessageType type) throws OnionApiException;

    /**
     * Send voice data received on a tunnel to the corresponding CM module.
     *
     * @param tunnelId The ID of the tunnel is data is meant for.
     * @param data The data to forward as voice data.
     * @throws OnionApiException If the channel is in an invalid state.
     */
    void sendVoiceData(int tunnelId, byte[] data) throws OnionApiException;
}
