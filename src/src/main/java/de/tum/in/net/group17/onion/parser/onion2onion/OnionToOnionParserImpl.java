package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.model.LidImpl;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by Christoph Rudolf on 21.06.17.
 * Implemented by Marko Dorfhuber (PraMiD) on 24.06.2017.
 *
 * Implements the OnionToOnionParser interface for the first version of the protocol.
 */
public class OnionToOnionParserImpl extends VoidphoneParser implements OnionToOnionParser {
    private final short lidLen = 16;

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public void setEncryptedRelayData(OnionTunnelRelayParsedMessage msg, byte[] data) {
        if(data == null || data.length < 1)
            throw new ParsingException("Encrypted data is too short!");
        msg.setEncryptedData(data);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public void setUnencryptedRelayData(OnionTunnelRelayParsedMessage msg, byte[] data) {
        ByteBuffer buffer;
        byte[] outgoingLidRaw, addressRaw, relayPayload;
        short port;
        boolean isIpv4;
        InetAddress address;

        // 9 = port, 1 Bit IP indicator, 7 Bit reserved, 4 Byte min addr len and 1 Byte minimal data
        if(data == null || data.length < lidLen + 9)
            throw new ParsingException("Data is too short to contain all necessary information");
        buffer = ByteBuffer.wrap(data);

        outgoingLidRaw = new byte[lidLen];
        buffer.get(outgoingLidRaw);
        Lid outgoingLid = checkFormatLid(outgoingLidRaw);

        port = buffer.getShort(lidLen);
        isIpv4 = (buffer.getShort(lidLen + 2) & (short)(1 << Short.SIZE - 1)) == 0;

        // Is data long enough to contain all data and a address of the given type?
        // 5 == 1 Byte data and 4 Byte reserved, IP Bit and port
        if(data.length < lidLen + 5 + (isIpv4 ? 4 : 16))
            throw new ParsingException("Data is too short to contain all necessary information");

        addressRaw = new byte[isIpv4 ? 4 : 16];
        buffer.position(lidLen + 4);
        buffer.get(addressRaw);
        try {
            address = InetAddress.getByAddress(addressRaw);
        } catch (UnknownHostException e) {
            throw new ParsingException("Data contains an invalid address!");
        }

        relayPayload = new byte[data.length - (lidLen + 4 + (isIpv4 ? 4 : 16))];
        buffer.position(lidLen + 4 + (isIpv4 ? 4 : 16));
        buffer.get(relayPayload);

        msg.setUnencryptedData(outgoingLid, address, port, data);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelInitMsg(byte[] incomingLidRaw, byte[] handshakePayload)
    {
        if(handshakePayload == null || handshakePayload.length < 1)
            throw new ParsingException("Handshake data is too short");

        return new OnionTunnelInitParsedMessage(checkFormatLid(incomingLidRaw), handshakePayload);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelAcceptMsg(byte[] incomingLidRaw, byte[] handshakePayload)
    {
        if(handshakePayload == null || handshakePayload.length < 1)
            throw new ParsingException("Handshake data is too short");

        return new OnionTunnelAcceptParsedMessage(checkFormatLid(incomingLidRaw), handshakePayload);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelRelayMsg(byte[] incomingLidRaw, byte[] outgoingLidRaw,
                                                  byte[] addressRaw, short port, byte[] data) {
        if(data == null || data.length < 1)
            throw new ParsingException("Relay data is too short");

        Lid incomingLid = checkFormatLid(incomingLidRaw); // Incoming on receiver side
        Lid outgoingLid = checkFormatLid(outgoingLidRaw); // New tunnel on reiceiver side

        try {
            InetAddress address = InetAddress.getByAddress(addressRaw);
            return new OnionTunnelRelayParsedMessage(incomingLid, outgoingLid, address, port, data);
        } catch (UnknownHostException e) {
            throw new ParsingException("Invalid IP address!");
        }
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelTransferMsg(byte[] incominLidRaw, byte[] data) {
        if(data == null || data.length < 1)
            throw new ParsingException("Data contained in ONION_TUNNEL_TRANSFER message is too short!");

        return new OnionTunnelTransportParsedMessage(checkFormatLid(incominLidRaw), data);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelTeardownMsg(byte[] incomingLidRaw, byte[] timestampBlob) {
        if(timestampBlob == null || timestampBlob.length < 1)
            throw new ParsingException("Timestamp data is too short!");

        return new OnionTunnelTeardownParsedMessage(checkFormatLid(incomingLidRaw), timestampBlob);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage parseMsg(byte[] data) {
        checkSize(data); // Throws an exception on all errors
        GenericMsgContent content;

        switch(extractType(data)) {
            case ONION_TUNNEL_INIT:
                content = parseIncomingOnionMessage(data, 1, MessageType.ONION_TUNNEL_INIT);
                return new OnionTunnelInitParsedMessage(content.lid, content.data);
            case ONION_TUNNEL_ACCEPT:
                content = parseIncomingOnionMessage(data, 1, MessageType.ONION_TUNNEL_ACCEPT);
                return new OnionTunnelAcceptParsedMessage(content.lid, content.data);
            case ONION_TUNNEL_RELAY:
                content = parseIncomingOnionMessage(data, 1, MessageType.ONION_TUNNEL_RELAY);
                return new OnionTunnelRelayParsedMessage(content.lid, content.data);
            case ONION_TUNNEL_TRANSPORT:
                content = parseIncomingOnionMessage(data, 1, MessageType.ONION_TUNNEL_TRANSPORT);
                return new OnionTunnelTransportParsedMessage(content.lid, content.data);
            case ONION_TUNNEL_TEARDOWN:
                content = parseIncomingOnionMessage(data, 1, MessageType.ONION_TUNNEL_TEARDOWN);
                return new OnionTunnelTeardownParsedMessage(content.lid, content.data);
            default:
                throw new ParsingException("Not able to parse message. Type: " + extractType(data).getValue() + "!");
        }
    }

    /**
     * Check the given Lid in Raw format for validity.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the tunnel to the receiver of the message.
     * @return The parsed Lid if all parameters are valid;
     */
    private Lid checkFormatLid(byte[] incomingLidRaw) {
        if(incomingLidRaw == null || incomingLidRaw.length < lidLen)
            throw new ParsingException("Lid too short!");

        try {
           return LidImpl.deserialize(incomingLidRaw);
        } catch(IllegalStateException e) {
            throw new ParsingException("Invalid LID!");
        }

    }

    /**
     * Parse all messages retrieved by the Onion module with respect to a given type, minimalDataLen, and a MessageType.
     * This method throws a ParsingException on every error!
     *
     * @param message The message to parse.
     * @param minDataLen The minimal length of contained data.
     * @param type The Type this message must have.
     * @return A GenericMsgContent containing the data and LID of the received message.
     */
    private GenericMsgContent parseIncomingOnionMessage(byte[] message, int minDataLen, MessageType type) {
        byte[] lidRaw;

        if(message.length < 4 + minDataLen + lidLen)
            throw new ParsingException("Message too short to contain a Onion message");
        checkType(message, type);

        lidRaw = new byte[lidLen];
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.position(4);
        buffer.get(lidRaw);

        return new GenericMsgContent(LidImpl.deserialize(lidRaw),
                Arrays.copyOfRange(message, 4 + lidLen, message.length));
    }

    /**
     * This class is used to return the Lid and the data contained in all incoming Onion messages.
     */
    private class GenericMsgContent {
        public final Lid lid;
        public final byte[] data;

        public GenericMsgContent(Lid lid, byte[] data) {
            this.lid = lid;
            this.data = data;
        }
    }
}
