package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 04.06.17.
 */
public class OnionTunnelIncomingParsedMessage extends ParsedMessage {
    private final int tunnelId;

    /**
     * Create a new ONION TUNNEL INCOMING message.
     * This object may only be created by an OnionParser.
     *
     * @param tunnelId The identifier of the new tunnel
     */
    protected OnionTunnelIncomingParsedMessage(int tunnelId) {
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
        return MessageType.ONION_TUNNEL_INCOMING;
    }

    /**
     * Return the tunnel ID of the newly created tunnel.
     *
     * @return ID of the new tunnel.
     */
    public int getTunnelId() {
        return tunnelId;
    }
}
