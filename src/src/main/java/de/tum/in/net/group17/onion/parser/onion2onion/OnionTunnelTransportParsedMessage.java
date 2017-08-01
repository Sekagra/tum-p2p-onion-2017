package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsingException;

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
    public static final int MAX_INNER_SIZE = 512;
    public static final byte[] MAGIC = "PtoP".getBytes();

    private byte[] data; // Inner packet including padding and magic prefix

    /**
     * Create a new ONION_TUNNEL_TRANSPORT message after checking all parameters.
     * This object may only be created by a OnionToOnionParser.
     * @param incomingLid The LID contained in this packet.
     * @param data The raw unecnrypted data transported using this message
     */
    OnionTunnelTransportParsedMessage(Lid incomingLid, byte[] data) {
        super(incomingLid);
        this.data = data;
    }

    /**
     * Get the complete data contained in this message.
     * This includes the magic block and padding.
     *
     * @return A byte[] containing the data transported by this message.
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Replace the data included in this message with for example its encryption.
     * This has to include the magic block and padding.
     *
     * @param data The new data for this message.
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Determine if this packet has to be processed by this peer. (Packet contains the PtoP magic)
     *
     * @return true if this peer has to process this message
     */
    public boolean forMe()
    {
        /* If the message data is still encrypted, we have no way to know where inside the byte block the magic bytes
         * are to be found, however, we suppose that after complete decryption, it is again the first 4 byte block of
         * the payload. */
        return Arrays.equals(MAGIC, Arrays.copyOfRange(this.data, 0, 4));
    }

    /**
     * Get the packet contained in the payload of this transport message.
     * Calling this method is only valid if this host is the receiver of the transport packet.
     * If the packet is not for this hop we will throw a IllegalStateException.
     *
     * We throw a IllegalDataException if data (msg + padding) has an invalid size!
     *
     * @return The packet contained in the ONION TUNNEL TRANSPORT payload.
     * @throws ParsingException If data (msg + padding) has an invalid size!
     */

    public byte[] getInnerPacket() throws ParsingException {
        if(!forMe())
            throw new IllegalStateException("This packet is not supposed for this peer." +
                    " Therefore, the inner packet is just garbage!");
        if(this.data.length != MAX_INNER_SIZE)
            throw new ParsingException("Invalid data length!");
        ByteBuffer buffer = ByteBuffer.wrap(this.data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        short size = buffer.getShort(); // Data contains another packet -> First two byte are the length
        return Arrays.copyOfRange(this.data, 0, size);
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = super.serializeBase();
        buffer.put(this.data);
        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(super.getSizeBase() + this.data.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_TRANSPORT;
    }
}
