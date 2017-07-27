package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.model.LidImpl;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;

import java.net.Inet4Address;
import java.net.Inet6Address;
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
    private final int lidLen = LidImpl.LENGTH;

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelInitMsg(byte[] incomingLidRaw, byte[] handshakePayload) throws ParsingException
    {
        if(handshakePayload == null || handshakePayload.length < 1)
            throw new ParsingException("Handshake data is too short");

        return new OnionTunnelInitParsedMessage(LidImpl.deserialize(incomingLidRaw), handshakePayload);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelAcceptMsg(byte[] incomingLidRaw, byte[] handshakePayload) throws ParsingException
    {
        if(handshakePayload == null || handshakePayload.length < 1)
            throw new ParsingException("Handshake data is too short");

        return new OnionTunnelAcceptParsedMessage(LidImpl.deserialize(incomingLidRaw), handshakePayload);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelRelayMsg(byte[] incomingLidRaw, byte[] outgoingLidRaw,
                                                  byte[] addressRaw, short port, byte[] data) throws ParsingException {
        if(data == null || data.length < 1)
            throw new ParsingException("Relay data is too short");

        Lid incomingLid = LidImpl.deserialize(incomingLidRaw); // Incoming on receiver side
        Lid outgoingLid = LidImpl.deserialize(outgoingLidRaw); // New tunnel on reiceiver side

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
    public ParsedMessage buildOnionTunnelTransferMsg(byte[] incominLidRaw, byte[] data) throws ParsingException {
        if(data == null || data.length < 1)
            throw new ParsingException("Data contained in ONION_TUNNEL_TRANSFER message is too short!");

        return new OnionTunnelTransportParsedMessage(LidImpl.deserialize(incominLidRaw), "PtoP".getBytes(), data);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelTeardownMsg(byte[] incomingLidRaw, byte[] timestampBlob) throws ParsingException {
        if(timestampBlob == null || timestampBlob.length < 1)
            throw new ParsingException("Timestamp data is too short!");

        return new OnionTunnelTeardownParsedMessage(LidImpl.deserialize(incomingLidRaw), timestampBlob);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelVoiceMsg(byte[] payload) throws ParsingException {
        if(payload.length + 4 > OnionTunnelVoiceParsedMessage.MAX_SIZE)
            throw new ParsingException("Payload too long!");

        return new OnionTunnelVoiceParsedMessage(payload);
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage parseMsg(byte[] data) throws ParsingException {
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
                return parseIncomingRelayMessage(data);
            case ONION_TUNNEL_TRANSPORT:
                return parseIncomingTransportMessage(data);
            case ONION_TUNNEL_TEARDOWN:
                content = parseIncomingOnionMessage(data, 1, MessageType.ONION_TUNNEL_TEARDOWN);
                return new OnionTunnelTeardownParsedMessage(content.lid, content.data);
            case ONION_TUNNEL_VOICE:
                return parseIncomingVoiceMessage(data);
            default:
                throw new ParsingException("Not able to parse message. Type: " + extractType(data).getValue() + "!");
        }
    }


    /**
     * Parse a ONION TUNNEL RELAY message.
     * The method throws a ParsingException on every parsing error!
     *
     * @param message Array containing the packet to parse.
     * @return OnionToOnionParseMessage of type ONION_TUNNEL_RELAY if the packet is a valid ONION TUNNEL RELAY message.
     */
    private OnionToOnionParsedMessage parseIncomingRelayMessage(byte[] message) throws ParsingException {
        GenericMsgContent genericHeader;
        byte[] lidRaw;
        short port;
        boolean isIpv4;
        InetAddress ipAddress;
        byte[] addr;
        int addrLen;

        try {
            genericHeader = parseIncomingOnionMessage(message,
                    4 + 4 + lidLen + 1, // port, res, IP, LID, data
                    MessageType.ONION_TUNNEL_RELAY);
        } catch(ParsingException e) {
            throw new ParsingException("Could not parse incoming ONION TUNNEL RELAY message. " + e.getMessage());
        }
        ByteBuffer buffer = ByteBuffer.wrap(message);

        port = buffer.getShort(4 + lidLen);
        isIpv4 = (buffer.getShort(6 + lidLen) & (short)(1 << 15)) == 0;
        addrLen = isIpv4 ? 4 : 16;

        try {
            buffer.position(24);
            if (isIpv4) {
                addr = new byte[4];
                buffer.get(addr, 0, 4);
                ipAddress = Inet4Address.getByAddress(addr);
            } else {
                addr = new byte[16];
                buffer.get(addr, 0, 16);
                ipAddress = Inet6Address.getByAddress(addr);
            }
        } catch(UnknownHostException e) {
            //Can not happen, but throw exception to avoid compiler warnings
            throw new ParsingException("Invalid IP in ONION TUNNEL RELAY message!");
        }

        lidRaw = new byte[lidLen];
        buffer.position(4 + lidLen + 4 + addrLen);
        buffer.get(lidRaw);

        return new OnionTunnelRelayParsedMessage(genericHeader.lid,
                LidImpl.deserialize(lidRaw),
                ipAddress,
                port,
                Arrays.copyOfRange(message, 4 + 2 * lidLen + 4 + addrLen, message.length));
    }


    /**
     * Parse a ONION TUNNEL TRANSPORT message.
     * The method throws a ParsingException on every parsing error!
     *
     * @param message Array containing the packet to parse.
     * @return OnionToOnionParseMessage of type ONION_TUNNEL_TRANSPORT if the packet is a valid ONION TUNNEL TRANSPORT message.
     */
    private OnionToOnionParsedMessage parseIncomingTransportMessage(byte[] message) throws ParsingException
    {
        GenericMsgContent genericHeader;

        try {
            genericHeader = parseIncomingOnionMessage(message,
                    4 + 1, // MAGIC + data
                    MessageType.ONION_TUNNEL_TRANSPORT);
        } catch(ParsingException e) {
            throw new ParsingException("Could not parse incoming ONION TUNNEL TRANSPORT message. " + e.getMessage());
        }

        // Just extract the magic from the data, as the rest is a BLOB for us
        return new OnionTunnelTransportParsedMessage(genericHeader.lid,
                Arrays.copyOfRange(genericHeader.data, 0, 4),
                Arrays.copyOfRange(genericHeader.data, 4, genericHeader.data.length));
    }

    private ParsedMessage parseIncomingVoiceMessage(byte[] message) throws ParsingException
    {
        checkType(message, MessageType.ONION_TUNNEL_VOICE);

        if(message.length > OnionTunnelVoiceParsedMessage.MAX_SIZE) {
            throw new ParsingException("Voice message is larger than "
                    + OnionTunnelVoiceParsedMessage.MAX_SIZE + " byte!");
        }

        if(message.length < 5)
            throw new ParsingException("Message to short!");

        return new OnionTunnelVoiceParsedMessage(Arrays.copyOfRange(message, 4, message.length));
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
    private GenericMsgContent parseIncomingOnionMessage(byte[] message, int minDataLen, MessageType type) throws ParsingException {
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