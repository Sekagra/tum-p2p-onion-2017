package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 05.06.17.
 */
public class OnionTunnelDestroyParsedMessage extends ParsedMessage {
    private final int tunnelId;

    /**
     * Create a ONION DESTORY MESSAGE.
     * This object may only be created by an OnionParser after checking the message.
     *
     * @param tunnelId The tunnel ID contained in the message.
     */
    protected OnionTunnelDestroyParsedMessage(int tunnelId) {
        this.tunnelId = tunnelId;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.putInt(tunnelId);
        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return 8;
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_DESTROY;
    }

    /**
     * Get the tunnel identifier contained in the message.
     *
     * @return The tunnel ID of the corresponding onion2onion tunnel.
     */
    public int getTunnelId() {
        return tunnelId;
    }
}
