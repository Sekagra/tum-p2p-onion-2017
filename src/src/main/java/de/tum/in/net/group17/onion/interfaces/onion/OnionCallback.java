package de.tum.in.net.group17.onion.interfaces.onion;

import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;

/**
 * Implements callback methods to notify a superordinate module of changes
 * that are relevant to other modules.
 * Created by Christoph Rudolf on 17.07.17.
 */
public interface OnionCallback {
    void tunnelAccepted(int tunnelId);
    void error();
    void tunnelData(int tunnelId, OnionTunnelDataParsedMessage msg);
}
