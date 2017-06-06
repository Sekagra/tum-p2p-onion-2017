package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.05.17.
 *
 * Implement a parser for the Onion API.
 * This class covers the first version of the Voidphone Onion API.
 */
public class OnionApiParserImpl extends VoidphoneParser implements OnionApiParser {

    /**
     * @inheritDoc
     *
     * This implementation throws an ParsingException on every error.
     */
    public ParsedMessage buildOnionErrorMsg(MessageType requestType, int tunnelId) {
        if (requestType != MessageType.ONION_TUNNEL_BUILD
                && requestType != MessageType.ONION_TUNNEL_DESTROY
                && requestType != MessageType.ONION_TUNNEL_DATA
                && requestType != MessageType.ONION_COVER)
            throw new ParsingException("Unknown request: " + requestType + "!");;

        return new OnionErrorParsedMessage(requestType, tunnelId);
    }

    /**
     * @inheritDoc
     *
     * The host key is checked for validity.
     * This implementation throws an ParsingException on every error.
     */
    public ParsedMessage buildOnionTunnelIncomingMsg(int id, ASN1Primitive sourceKey) {
        try {
            int size = 8 + sourceKey.getEncoded().length; // Header, TunnelID and key
            if (size > 65535)
                throw new ParsingException("Message too large!");
        } catch(IOException e) {
            throw new ParsingException("Invalid source key.");
        }

        return new OnionTunnelIncomingParsedMessage(id, sourceKey);
    }

    /**
     * @inheritDoc
     *
     * We check the destination key for validity.
     * This implementation throws an ParsingException on every error.
     */
    public ParsedMessage buildOnionTunnelReadyMsg(int id, ASN1Primitive destinationKey) {
        try {
            int size = 8 + destinationKey.getEncoded().length; // Header, TunnelID and destination key
            if (size > 65535)
                throw new ParsingException("Message too large!");
        } catch(IOException e) {
            throw new ParsingException("Invalid destination key!");
        }

        return new OnionTunnelReadyParsedMessage(id, destinationKey);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws an ParsingException on every error.
     */
    public ParsedMessage parseMsg(byte[] data) {
        checkSize(data); // Throws an exception on all errors

        switch(extractType(data)) {
            case ONION_TUNNEL_BUILD:
                return parseOnionTunnelBuildMsg(data);
            case ONION_TUNNEL_DESTROY:
                return parseOnionTunnelDestroyMsg(data);
            case ONION_TUNNEL_DATA:
                return parseOnionTunnelDataMsg(data);
            case ONION_COVER:
                return parseOnionCoverMsg(data);
            default:
                throw new ParsingException("Not able to parse message. Type: " + extractType(data).getValue() + "!");
        }
    }

    /**
     * Parse an incoming ONION COVER message.
     * This method will throw an ParsingException if the message length does not match to the specified ONION
     * COVER message length(8).
     *
     * @param data The actual packet.
     * @return A ParsedMessage on success.
     */
    private ParsedMessage parseOnionCoverMsg(byte[] data) {
        ByteBuffer buffer;

        if(data.length != 8)
            throw new ParsingException("An ONION COVER message must have exactly 8 bytes!");
        checkType(data, MessageType.ONION_COVER); // Will throw a parsing exception on any error

        buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.position(2);
        buffer.position(4);
        short coverSize = buffer.getShort();

        return new OnionCoverParsedMessage(coverSize);
    }

    /**
     * Parse an ONION TUNNEL BUILD message.
     *
     * @param data The packet.
     * @return An ParsedMessage if parsing was successful.
     */
    private ParsedMessage parseOnionTunnelBuildMsg(byte[] data) {
        ByteBuffer buffer;

        checkType(data, MessageType.ONION_TUNNEL_BUILD); // Will throw a parsing exception on any error
        // Length is unknown => No check possible

        buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.position(6);
        short port = buffer.getShort();

        // TODO: Add support for IPv6 AND IPv4 parsing!
        byte[] addressRaw = new byte[4];
        buffer.position(8);
        buffer.get(addressRaw);
        InetAddress address;

        try {
            address = InetAddress.getByAddress(addressRaw);
        } catch(UnknownHostException e) {
            throw new ParsingException("Cannot parse the IP address!");
        }

        byte[] keyRaw = new byte[data.length - 12];
        buffer.position(12);
        buffer.get(keyRaw);
        ASN1Primitive key;

        try {
            key = new ASN1InputStream(new ByteArrayInputStream(keyRaw)).readObject().toASN1Primitive();
        } catch (IOException e) {
            throw new ParsingException("Invalid hostkey!");
        }

        return new OnionTunnelBuildParsedMessage(port, address, key);
    }

    /**
     * Check the validity of a ONION TUNNEL DESTROY message. This is basically only the packet's length..
     * The method will throw an ParsingException if a parsion error occurs.
     *
     * @param data The actual packet.
     * @return A ParsedMessage if the packet is valid.
     */
    private ParsedMessage parseOnionTunnelDestroyMsg(byte[] data) {
        ByteBuffer buffer;
        if(data.length != 8)
            throw new ParsingException("A ONION TUNNEL DESTROY message must have exactly 8 bytes!");
        checkType(data, MessageType.ONION_TUNNEL_DESTROY); // Will throw a parsing exception on any error

        buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.position(4);
        int tunnelId = buffer.getInt();

        return new OnionTunnelDestroyParsedMessage(tunnelId);
    }

    /**
     * Parse a ONION TUNNEL DATA message. We can only check if the packet is long enough to contain any data.
     * This method will throw an ParsingException on any parsing error.
     *
     * @param data The actual packet
     * @return A ParsedMessage if the ONION TUNNEL DATA packet is valid.
     */
    private ParsedMessage parseOnionTunnelDataMsg(byte[] data) {
        ByteBuffer buffer;

        if(data.length < 9)
            throw new ParsingException("The packet is too short to contain any data!");
        checkType(data, MessageType.ONION_TUNNEL_DATA); // Will throw a parsing exception on any error

        buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        // TODO: Change to version without .position in every call -> Nicer to read
        int tunnelId = buffer.getShort();
        byte[] msgData = new byte[data.length - 8];
        buffer.get(msgData, 0, data.length - 8);

        return new OnionTunnelDataParsedMessage(tunnelId, msgData);
    }
}
