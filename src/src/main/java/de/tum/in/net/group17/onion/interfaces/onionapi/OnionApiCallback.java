package de.tum.in.net.group17.onion.interfaces.onionapi;

import de.tum.in.net.group17.onion.parser.onionapi.OnionCoverParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelBuildParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDestroyParsedMessage;

/**
 * Implements callback methods to notify a superordinate module of changes
 * that are relevant to other modules.
 * Created by Christoph Rudolf on 17.07.17.
 */
public interface OnionApiCallback {
    /**
     * Handle a ONION TUNNEL BUILD message.
     *
     * @param msg The message received on the API.
     */
    void receivedTunnelBuild(OnionTunnelBuildParsedMessage msg);

    /**
     * Handle a ONION COVER message.
     *
     * @param msg The message received on the API.
     */
    void receivedCoverData(OnionCoverParsedMessage msg);

    /**
     * Handle a ONION TUNNEL DATA message.
     *
     * @param msg The message received on the API.
     */
    void receivedVoiceData(OnionTunnelDataParsedMessage msg);

    /**
     * Handle a ONION TUNNEL DESTROY message.
     *
     * @param msg The message received on the API.
     */
    void receivedDestroy(OnionTunnelDestroyParsedMessage msg);
}
