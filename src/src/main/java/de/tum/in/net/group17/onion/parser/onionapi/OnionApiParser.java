package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * Created by Christoph Rudolf on 24.05.17.
 * Implementation by Marko Dorfhuber(PraMiD) on 25.05.17.
 *
 * Provides an interface to handle Onion API messages.
 */
public interface OnionApiParser {

    /**
     * Create an ONION ERROR message. Nothing can go wrong here.
     *
     * param requestType The request type of the request causing the error.
     * @param tunnelID The ID of the tunnel where the error occured.
     * @return An ParsedMessage containing the ONION ERROR message.
     */
    ParsedMessage buildOnionErrorMsg(short requestType, int tunnelID);

    /**
     * Create a OnionTunnelIncoming message containing the given parameters.
     *
     * @param id ID of the tunnel.
     * @param sourceKey Key of the source host.
     * @return A ParsedMessage of creation was successful.
     */
    ParsedMessage buildOnionTunnelIncoming(int id, byte[] sourceKey);

    /**
     * Create a OnionParseObject containing a ONION TUNNEL READY message with the given parameters.
     *
     * @param id The ID of the newly created tunnel.
     * @param destinationKey The destination host's key.
     * @return A ParsedMessage containing the message on success.
     */
    ParsedMessage buildOnionTunnelReadyMsg(int id, byte[] destinationKey);

    /**
     * Parse an incomning message to the Onion module.
     *
     * @param data The actual packet.
     * @return A ParsedMessage containing the message type and the data if parsing was successful.
     */
    ParsedMessage parseMsg(byte[] data);
}
