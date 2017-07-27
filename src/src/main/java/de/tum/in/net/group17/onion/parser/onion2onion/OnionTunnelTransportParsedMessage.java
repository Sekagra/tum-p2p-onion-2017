package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.06.17.
 *
 * This class represents a ONION_TUNNEL_TRANSPORT parsed message.
 * Objects of this class may only be created by a OnionToOnionParser after checking all parameters.
 */
public class OnionTunnelTransportParsedMessage extends OnionToOnionParsedMessage {
    public static final byte[] MAGIC = "PtoP".getBytes();

    private final byte[] data; // Inner packet including padding
    private final byte[] magic; // The (possibly encrypted) magic word

    /**
     * Create a new ONION_TUNNEL_TRANSPORT message after checking all parameters.
     * This object may only be created by a OnionToOnionParser.
     * @param incoming_lid The LID contained in this packet.
     * @param magic The (possibly encrypted) magic word
     * @param data The data transported using this message
     */
    OnionTunnelTransportParsedMessage(Lid incoming_lid, byte[] magic, byte[] data) {
        super(incoming_lid);
        this.magic = magic;
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
     * Determine if this packet has to be processed by this peer. (Packet contains the PtoP magic)
     *
     * @return true if this peer has to process this message
     */
    public boolean forMe()
    {
        return Arrays.equals(MAGIC, this.magic);
    }


    /**
     * Get the packet contained in the payload of this transport message.
     * Calling this method is only valid if this host is the receiver of the transport packet.
     * If the packet is not for this hop we will throw a IllegalStateException.
     *
     * @return The packet contained in the ONION TUNNEL TRANSPORT payload.
     */
    public byte[] getInnerPacket()
    {
        if(!forMe())
            throw new IllegalStateException("This packet is not supposed for this peer." +
                    " Therefore, the inner packet is just garbage!");
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        short size = buffer.getShort(); // Data contains another packet -> First two byte are the length
        return Arrays.copyOfRange(data, 0, size);
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = super.serializeBase();

        buffer.put(magic);
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
