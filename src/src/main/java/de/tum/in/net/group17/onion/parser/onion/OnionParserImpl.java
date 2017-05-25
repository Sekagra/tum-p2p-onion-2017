package de.tum.in.net.group17.onion.parser.onion;

import org.bouncycastle.asn1.ASN1InputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.05.17.
 *
 * Implement a parser for the Onion API.
 * This class covers the first version of the Voidphone Onion API.
 */
public class OnionParserImpl implements OnionParser {

    /**
     * Create an ONION ERROR message. Nothing can go wrong here.
     *
     * param requestType The request type of the request causing the error.
     * @param tunnelID The ID of the tunnel where the error occured.
     * @return An OnionParsedObject containing the ONION ERROR message.
     */
    public OnionParsedObject buildOnionErrorMsg(short requestType, int tunnelID) {
        // TODO: May check the request type
        ByteBuffer buffer = ByteBuffer.allocate(12);

        buffer.putShort((short)12);
        buffer.putShort((short)OnionParsedObject.ONION_MSG_TYPE.ONION_ERROR.getValue());
        buffer.putShort(requestType);
        buffer.putShort((short)0);
        buffer.putInt(tunnelID);

        return new OnionParsedObject(buffer.array(), OnionParsedObject.ONION_MSG_TYPE.ONION_ERROR);
    }

    /**
     * Create a OnionTunnelIncoming message containing the given parameters.
     * This method will throw an IllegalArgumentException on every parsing errors.
     *      (We check the source key for validity).
     *
     * @param id ID of the tunnel.
     * @param sourceKey Key of the source host.
     * @return A OnionParsedObject of creation was successful.
     */
    public OnionParsedObject buildOnionTunnelIncoming(int id, byte[] sourceKey) {
        int size = 8 + sourceKey.length; // Header, TunnelID and hostkey
        if (size > 65535)
            throw new IllegalArgumentException("Message too large!");

        try {
            new ASN1InputStream(new ByteArrayInputStream(sourceKey)).readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid source key!");
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);
        buffer.putShort((short)OnionParsedObject.ONION_MSG_TYPE.ONION_TUNNEL_INCOMING.getValue());
        buffer.put(sourceKey);

        return new OnionParsedObject(buffer.array(), OnionParsedObject.ONION_MSG_TYPE.ONION_TUNNEL_INCOMING);
    }

    /**
     * Create a OnionParseObject containing a ONION TUNNEL READY message with the given parameters.
     * This method will throw an IllegalArgumentException of any parsing error.
     *      (We check the destination key for validity)
     *
     * @param id The ID of the newly created tunnel.
     * @param destinationKey The destination host's key.
     * @return A OnionParsedObject containing the message on success.
     */
    public OnionParsedObject buildOnionTunnelReadyMsg(int id, byte[] destinationKey) {
        int size = 8 + destinationKey.length; // Header, TunnelID and hostkey
        if (size > 65535)
            throw new IllegalArgumentException("Message too large!");

        try {
            new ASN1InputStream(new ByteArrayInputStream(destinationKey)).readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid destination key!");
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);
        buffer.putShort((short)OnionParsedObject.ONION_MSG_TYPE.ONION_TUNNEL_READY.getValue());
        buffer.put(destinationKey);

        return new OnionParsedObject(buffer.array(), OnionParsedObject.ONION_MSG_TYPE.ONION_TUNNEL_READY);
    }

    /**
     * Parse an incomning message to the Onion module.
     * The method will throw an IllegalArgumentExcaption on every parsing error!
     *
     * @param data The actual packet.
     * @return A OnionParsedObject containing the message typt and the data if parsing was successful.
     */
    public OnionParsedObject parseMsg(byte[] data) {
        if(data.length > 65366)
            throw new IllegalArgumentException("Packet too large!");
        if(data.length < 4)
            throw new IllegalArgumentException("Packet must at least contain the header!");

        ByteBuffer buffer = ByteBuffer.wrap(data);
        int size = buffer.getShort(0);
        if(size != data.length)
            throw new IllegalArgumentException("Length in header does not match packet length!");

        OnionParsedObject.ONION_MSG_TYPE type;
        try {
            type = OnionParsedObject.ONION_MSG_TYPE.values()[buffer.getShort(2)];
        } catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unknown message type: " + buffer.getShort(2) + "!");
        }

        switch(type) {
            case ONION_TUNNEL_BUILD:
                return parseOnionTunnelBuildMsg(data);
            case ONION_TUNNEL_DESTROY:
                return parseOnionTunnelDestroyMsg(data);
            case ONION_TUNNEL_DATA:
                return parseOnionTunnelDataMsg(data);
            case ONION_COVER:
                return parseOnionCoverMsg(data);
            default:
                throw new IllegalArgumentException("Not able to parse message. Type: " + type.getValue() + "!");
        }
    }

    /**
     * Parse an incoming ONION COVER message.
     * This method will throw an IllegalArgumentException if the message length does not match to the specified ONION
     * COVER message length(8).
     *
     * @param data The actual packet.
     * @return A OnionParsedObject on success.
     */
    private OnionParsedObject parseOnionCoverMsg(byte[] data) {
        if(data.length != 8)
            throw new IllegalArgumentException("An ONION COVER message must have exaclty 8 bytes!");

        return new OnionParsedObject(data, OnionParsedObject.ONION_MSG_TYPE.ONION_COVER);
    }

    /**
     * Parse an ONION TUNNEL BUILD message.
     *
     * @param data The packet.
     * @return An OnionParsedObject if parsing was successful.
     */
    private OnionParsedObject parseOnionTunnelBuildMsg(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // TODO: Add support for IPv6 AND IPv4 parsing!
        byte[] address = new byte[4];
        buffer.get(address, 9, 4);

        try {
            InetAddress.getByAddress(address);
        } catch(UnknownHostException e) {
            throw new IllegalArgumentException("Cannot parse the IP address!");
        }

        byte[] key = new byte[data.length - 12];
        buffer.get(key, 12, data.length - 12);

        try {
            new ASN1InputStream(new ByteArrayInputStream(key)).readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid hostkey!");
        }

        return new OnionParsedObject(data, OnionParsedObject.ONION_MSG_TYPE.ONION_TUNNEL_BUILD);
    }

    /**
     * Check the validity of a ONION TUNNEL DESTROY message. This is basically only the packet's length..
     * The method will throw an IllegalArgumentException if a parsion error occurs.
     *
     * @param data The actual packet.
     * @return A OnionParsedObject if the packet is valid.
     */
    private OnionParsedObject parseOnionTunnelDestroyMsg(byte[] data) {
        if(data.length != 8)
            throw new IllegalArgumentException("A ONION TUNNEL DESTROY message must have exactly 8 bytes!");

        return new OnionParsedObject(data, OnionParsedObject.ONION_MSG_TYPE.ONION_TUNNEL_DESTROY);
    }

    /**
     * Parse a ONION TUNNEL DATA message. We can only check if the packet is long enough to contain any data.
     * This method will throw an IllegalArgumentException on any parsing error.
     *
     * @param data The actual packet
     * @return A OnionParsedObject if the ONION TUNNEL DATA packet is valid.
     */
    private OnionParsedObject parseOnionTunnelDataMsg(byte[] data) {
        if(data.length < 9)
            throw new IllegalArgumentException("The packet is too short to contain any data!");

        return new OnionParsedObject(data, OnionParsedObject.ONION_MSG_TYPE.ONION_TUNNEL_DATA);
    }
}
