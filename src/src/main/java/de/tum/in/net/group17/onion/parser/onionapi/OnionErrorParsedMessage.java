package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 04.06.17.
 */
public class OnionErrorParsedMessage extends ParsedMessage {
    private final MessageType requestType;
    private final int tunnelId;

    /**
     * Create a new ONION ERROR message.
     * This object may only be created using a OnionParser.
     *
     * @param reqType The type of the message triggering the error.
     * @param tunnelId The onion tunnel that is related to the tunnel.
     */
    OnionErrorParsedMessage(MessageType reqType, int tunnelId) {
        this.requestType = reqType;
        this.tunnelId = tunnelId;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putShort(requestType.getValue());
        buffer.putShort((short)0); // Reserved
        buffer.putInt(tunnelId);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return 12;
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_ERROR;
    }

    /**
     * Get the message type that triggered the error.
     *
     * @return Type of the message triggering the error.
     */
    public MessageType getRequestType() {
        return requestType;
    }

    /**
     * Get the tunnel which is related to the error.
     *
     * @return The tunnel related to the error.
     */
    public int getTunnelId() {
        return tunnelId;
    }
}
