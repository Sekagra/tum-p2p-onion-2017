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
    private final ASN1Primitive sourceKey;
    private final int tunnelId;

    /**
     * Create a new ONION TUNNEL INCOMING message.
     * This object may only be created by an OnionParser.
     *
     * @param tunnelId The identifier of the new tunnel
     * @param sourceKey The initiating host's key.
     */
    protected OnionTunnelIncomingParsedMessage(int tunnelId, ASN1Primitive sourceKey) {
        this.tunnelId = tunnelId;
        this.sourceKey = sourceKey;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.putInt(tunnelId);
        try {
            buffer.put(sourceKey.getEncoded());
        } catch(IOException e) {
            // Checked beforehand!
            throw new Error("Invalid source key!");
        }

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        try {
            return (short)(8 + sourceKey.getEncoded().length);
        } catch(IOException e) {
            // Checked beforehand!
            throw new Error("Invalid source key!");
        }
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_INCOMING;
    }

    /**
     * Return the source key contained in this message.
     *
     * @return The contained source key.
     */
    public ASN1Primitive getSourceKey() {
        return sourceKey;
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
