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
    void receivedTunnelBuild(OnionTunnelBuildParsedMessage msg);
    void receivedCoverData(OnionCoverParsedMessage msg);
    void receivedVoiceData(OnionTunnelDataParsedMessage msg);
    void receviedDestroy(OnionTunnelDestroyParsedMessage msg);
}
