package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.Parser;
import de.tum.in.net.group17.onion.parser.ParsingException;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * Created by Christoph Rudolf on 24.05.17.
 * Implementation by Marko Dorfhuber(PraMiD) on 25.05.17.
 *
 * Provides an interface to handle Onion API messages.
 */
public interface OnionApiParser extends Parser {

    /**
     * Create an ONION ERROR message. Nothing can go wrong here.
     *
     * @param requestType The type of the request causing the error.
     * @param tunnelID The ID of the tunnel where the error occured.
     *
     * @return An ParsedMessage containing the ONION ERROR message.
     */
    ParsedMessage buildOnionErrorMsg(MessageType requestType, int tunnelID) throws ParsingException;

    /**
     * Create a OnionTunnelIncoming message containing the given parameters.
     *
     * @param id ID of the tunnel.
     * @param sourceKey Key of the source host.
     * @return A ParsedMessage of creation was successful.
     */
    ParsedMessage buildOnionTunnelIncomingMsg(int id, byte[] sourceKey) throws ParsingException;

    /**
     * Create a OnionParseObject containing a ONION TUNNEL READY message with the given parameters.
     *
     * @param id The ID of the newly created tunnel.
     * @param destinationKey The destination host's key.
     * @return A ParsedMessage containing the message on success.
     */
    ParsedMessage buildOnionTunnelReadyMsg(int id, byte[] destinationKey) throws ParsingException;

    /**
     * Create an ONION DATA message.
     *
     * @param tunnelID The ID of the tunnel where the error occured.
     * @param data The raw data to be sent to the call module.
     * @return An ParsedMessage containing the ONION ERROR message.
     */
    ParsedMessage buildOnionDataMsg(int tunnelID, byte[] data) throws ParsingException;
}
