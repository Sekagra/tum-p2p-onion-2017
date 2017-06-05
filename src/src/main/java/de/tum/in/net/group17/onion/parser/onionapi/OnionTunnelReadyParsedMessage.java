package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 04.06.17.
 */
public class OnionTunnelReadyParsedMessage extends ParsedMessage {
    private final ASN1Primitive destinationKey;
    private final int tunnelId;

    /**
     * Build a new ONION TUNNEL READY message containing the given parameters.
     *
     * @param tunnelId ID of the tunnel.
     * @param dstKey Key of the tunnel's destination host.
     */
    protected OnionTunnelReadyParsedMessage(int tunnelId, ASN1Primitive dstKey) {
        this.destinationKey = dstKey;
        this.tunnelId = tunnelId;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.putInt(tunnelId);
        try {
            buffer.put(destinationKey.getEncoded());
        } catch(IOException e) {
            // Should be checked beforehand!
            throw new Error("Invalid destination key!");
        }

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        try {
            return (short)(8 + destinationKey.getEncoded().length);
        } catch(IOException e) {
            // Should be checked beforehand!
            throw new Error("Invalid destination key!");
        }
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_READY;
    }

    /**
     * Return the destination host's key.
     *
     * @return ASN1Primitive containing the destination host's key.
     */
    public ASN1Primitive getDestinationKey() {
        return destinationKey;
    }

    /**
     * Return the tunnel identifier.
     *
     * @return The identifier of the corresponding tunnel.
     */
    public int getTunnelId() {
        return tunnelId;
    }
}
