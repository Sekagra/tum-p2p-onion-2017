package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.06.17.
 *
 * This class represents a ONION_TUNNEL_TEARDOWN message.
 * Objects of this class may only be created by OnionToOnionParsers.
 */
public class OnionTunnelTeardownParsedMessage extends OnionToOnionParsedMessage {
    public final byte[] encTimestamp;

    /**
     * Create a new ONION_TUNNEL_TEARDONW message after checking all parameters.
     * This object may only be created by a OnionToOnionParser.
     *
     * @param incomingLid The LID of the tunnel between the initiator and the receiver.
     * @param encryptedTimestamp Encrypted and integrity protected timestamp.
     */
    OnionTunnelTeardownParsedMessage(Lid incomingLid, byte[] encryptedTimestamp) {
        super(incomingLid);
        this.encTimestamp = encryptedTimestamp;
    }

    /**
     * Return the binary BLOB containing the encrypted and integrity protected timestamp of this message.
     *
     * @return A byte[] representing the encrypted timestamp.
     */
    public byte[] getEncryptedTimestamp() {
        return encTimestamp;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.wrap(super.serializeBase());

        buffer.put(encTimestamp);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(encTimestamp.length + super.getSizeBase());
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_TEARDOWN;
    }
}
