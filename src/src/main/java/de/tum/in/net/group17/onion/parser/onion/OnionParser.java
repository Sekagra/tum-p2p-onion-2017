package de.tum.in.net.group17.onion.parser.onion;

/**
 * Created by Christoph Rudolf on 24.05.17.
 * Implementation by Marko Dorfhuber(PraMiD) on 25.05.17.
 *
 * Providing a interface to handle Onion API messages.
 */
public interface OnionParser {

    /**
     * Create an ONION ERROR message.
     */
    OnionParsedObject buildOnionErrorMsg(short requestType, int tunnelID);

    /**
     * Create a OnionTunnelIncoming message.
     * The method shall throw an IllegalArgumentExcaption on every parsing error!
     */
    OnionParsedObject buildOnionTunnelIncoming(int id, byte[] sourceKey);

    /**
     * Create a OnionParseObject containing a ONION TUNNEL READY message.
     * The method shall throw an IllegalArgumentExcaption on every parsing error!
     */
    OnionParsedObject buildOnionTunnelReadyMsg(int id, byte[] destinationKey);

    /**
     * Parse an incomning message to the Onion module.
     * The method shall throw an IllegalArgumentExcaption on every parsing error!
     */
    OnionParsedObject parseMsg(byte[] data);
}
