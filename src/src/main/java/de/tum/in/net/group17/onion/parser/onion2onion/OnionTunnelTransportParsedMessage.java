package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.06.17.
 *
 * This class represents a ONION_TUNNEL_TRANSPORT parsed message.
 * Objects of this class may only be created by a OnionToOnionParser after checking all parameters.
 */
public class OnionTunnelTransportParsedMessage extends OnionToOnionParsedMessage {
    private final byte[] data;

    /**
     * Create a new ONION_TUNNEL_TRANSPORT message after checking all parameters.
     * This object may only be created by a OnionToOnionParser.
     * @param incoming_lid
     * @param data
     */
    OnionTunnelTransportParsedMessage(Lid incoming_lid, byte[] data) {
        super(incoming_lid);
        this.data = data;
    }

    /**
     * Get the data contained in this message.
     * This data has to be forwarded to the next hop if we are not the receiver.
     *
     * @return A byte[] containing the data transported by this message.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.wrap(super.serializeBase());

        buffer.put(data);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(super.getSizeBase() + data.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_TRANSPORT;
    }
}
