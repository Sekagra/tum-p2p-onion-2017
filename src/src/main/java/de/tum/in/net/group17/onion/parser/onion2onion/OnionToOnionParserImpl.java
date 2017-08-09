package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.model.LidImpl;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;
import org.bouncycastle.util.Arrays;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public ParsedMessage buildOnionTunnelTransferMsgPlain(byte[] incomingLidRaw, ParsedMessage innerPkt) throws ParsingException {
        if(innerPkt.getSize() > OnionTunnelTransportParsedMessage.MAX_INNER_SIZE)
            throw new ParsingException("Inner packet too large!");
        int paddingSize = OnionTunnelTransportParsedMessage.MAX_INNER_SIZE - innerPkt.getSize() - OnionTunnelTransportParsedMessage.MAGIC.length;
        if(paddingSize == 0) {
            return new OnionTunnelTransportParsedMessage(LidImpl.deserialize(incomingLidRaw),
                    Arrays.concatenate("PtoP".getBytes(), innerPkt.serialize()));
        }
        byte[] padding = new byte[paddingSize];
        new Random().nextBytes(padding);

        return new OnionTunnelTransportParsedMessage(LidImpl.deserialize(incomingLidRaw),
                Arrays.concatenate("PtoP".getBytes(), innerPkt.serialize(), padding));
    }


    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelTeardownMsg(byte[] incomingLidRaw) throws ParsingException {
        return new OnionTunnelTeardownParsedMessage(LidImpl.deserialize(incomingLidRaw));
    }

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingError on every error!
     */
    @Override
    public ParsedMessage buildOnionTunnelVoiceMsg(byte[] incomingLidRaw, byte[] payload) throws ParsingException {
        if(payload.length + 4 + lidLen > OnionTunnelTransportParsedMessage.MAX_INNER_SIZE)
            throw new ParsingException("Payload too long!");

        return new OnionTunnelVoiceParsedMessage(LidImpl.deserialize(incomingLidRaw), payload);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ParsedMessage buildOnionTunnelEstablishedMsg(byte[] lidRaw) throws ParsingException {
        return new OnionTunnelEstablishedParsedMessage(LidImpl.deserialize(lidRaw));
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
                content = parseIncomingOnionMessage(data, 0, MessageType.ONION_TUNNEL_TEARDOWN);
                return new OnionTunnelTeardownParsedMessage(content.lid);
            case ONION_TUNNEL_VOICE:
                content = parseIncomingOnionMessage(data, 1, MessageType.ONION_TUNNEL_VOICE);
                return new OnionTunnelVoiceParsedMessage(content.lid, content.data);
            case ONION_TUNNEL_ESTABLISHED:
                content = parseIncomingOnionMessage(data, 0, MessageType.ONION_TUNNEL_ESTABLISHED);
                return new OnionTunnelEstablishedParsedMessage(content.lid);
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
            // We do not know the exact equal size as we do not control ONION AUTH encryption but, we can check for a
            // minimal length!
            genericHeader = parseIncomingOnionMessage(message,
                    OnionTunnelTransportParsedMessage.MAX_INNER_SIZE, // MAGIC + data
                    MessageType.ONION_TUNNEL_TRANSPORT);
        } catch(ParsingException e) {
            throw new ParsingException("Could not parse incoming ONION TUNNEL TRANSPORT message. " + e.getMessage());
        }

        // Just extract the magic from the data, as the rest is a BLOB for us
        return new OnionTunnelTransportParsedMessage(genericHeader.lid, genericHeader.data);
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
            throw new ParsingException("Message too short to contain an Onion message");
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

    /**
     * @inheritDoc
     */
    @Override
    public List<ParsedMessage> buildOnionTunnelVoiceMsgs(byte[] incomingLidRaw, byte[] payload) throws ParsingException {
        List<ParsedMessage> result = new ArrayList<>();
        int partSize = OnionTunnelTransportParsedMessage.MAX_INNER_SIZE - lidLen - OnionTunnelTransportParsedMessage.MAGIC.length - 4; // subtract header

        int start = 0;
        while (start < payload.length) {
            int end = Math.min(payload.length, start + partSize);
            result.add(this.buildOnionTunnelVoiceMsg(incomingLidRaw, Arrays.copyOfRange(payload, start, end)));
            start += partSize;
        }

        return result;
    }
}