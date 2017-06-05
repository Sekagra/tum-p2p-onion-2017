package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 05.06.17.
 */
public class OnionTunnelDataParsedMessage extends ParsedMessage {
    private final int tunnelId;
    private final byte[] data;

    /**
     * Create a new ONION TUNNEL DATA message with the given parameters.
     * This object shall only be created by an OnionParser after checking the message.
     *
     * @param tunnelId The ID of the corresponding tunnel.
     * @param data The data which was contained in the parsed message.
     */
    public OnionTunnelDataParsedMessage(int tunnelId, byte[] data) {
        this.tunnelId = tunnelId;
        this.data = data;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.putInt(tunnelId);
        buffer.put(data);
        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(8 + data.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_DATA;
    }

    /**
     * Get the identifier of the corresponding tunnel.
     *
     * @return The tunnel ID contained in the message.
     */
    public int getTunnelId() {
        return tunnelId;
    }

    /**
     * Get the data send with the message.
     *
     * @return The data contained in the message.
     */
    public byte[] getData() {
        return data;
    }
}
