package de.tum.in.net.group17.onion.interfaces.onionapi;

import de.tum.in.net.group17.onion.parser.onionapi.OnionErrorParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelIncomingParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelReadyParsedMessage;

/**
 * This interface is responsible for serving incoming requests of the UI/CM thus essentially providing the Onion API
 * given in the specification of the application.
 * Created by Christoph Rudolf on 11.06.17.
 */
public interface OnionApiInterface {
    void listen(OnionApiCallback callback);

    /**
     * Inform the client (The last one that connected to the API) that a new tunnel was established to out peer.
     *
     * @param msg The message to send to the client.
     * @throws OnionApiException If the channel is in an invalid state.
     */
    void sendIncoming(OnionTunnelIncomingParsedMessage msg) throws OnionApiException;

    /**
     * Inform the client that a given tunnel is established.
     *
     * @param msg The message to send to the client.
     * @throws OnionApiException If the channel is in an invalid state.
     */
    void sendReady(OnionTunnelReadyParsedMessage msg) throws OnionApiException;

    /**
     * Send a error message to one of our clients.
     *
     * @param msg The message to send.
     * @throws OnionApiException If the channel is in an invalid state.
     */
    void sendError(OnionErrorParsedMessage msg) throws OnionApiException;
}
