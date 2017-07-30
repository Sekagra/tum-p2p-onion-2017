package de.tum.in.net.group17.onion.parser.onion2onion;

import com.sun.media.sound.InvalidDataException;
import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.DataFormatException;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.06.17.
 *
 * This class represents a ONION_TUNNEL_TRANSPORT parsed message.
 * Objects of this class may only be created by a OnionToOnionParser after checking all parameters.
 */
public class OnionTunnelTransportParsedMessage extends OnionToOnionParsedMessage {
    public static final int MAX_INNER_SIZE = 512;
    public static final byte[] MAGIC = "PtoP".getBytes();

    private final byte[] payload; // Inner packet including padding

    /**
     * Create a new ONION_TUNNEL_TRANSPORT message after checking all parameters.
     * This object may only be created by a OnionToOnionParser.
     * @param incoming_lid The LID contained in this packet.
     * @param payload The raw data transported using this message
     */
    OnionTunnelTransportParsedMessage(Lid incoming_lid, byte[] payload) {
        super(incoming_lid);
        this.payload = payload;
    }

    /**
     * Get the complete data contained in this message.
     * This includes the magic block and padding.
     *
     * @return A byte[] containing the data transported by this message.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Get the data of this message that has been created by the authentication module. This consists of the
     * payload without the random padding at the end.
     *
     * @return Returns AUTH([MAGIC] + [inner packet]) of the whole payload.
     */
    public byte[] getData() {
        throw new UnsupportedOperationException("Not implemented yet.");
        /* Here we would need to extract everything besides the trailing random padding from the given payload
         * This is necessary to hand the data to the Auth module for de- or encryption.
         * The padding itself cannot be part of the encryption as the whole purpose of the padding is to create a fixed
         * length AFTER the overhead of the encryption by AUTH is determined.
         */
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
        return Arrays.equals(MAGIC, Arrays.copyOfRange(this.payload, 0, 4));
    }


    /**
     * Get the packet contained in the payload of this transport message.
     * Calling this method is only valid if this host is the receiver of the transport packet.
     * If the packet is not for this hop we will throw a IllegalStateException.
     *
     * We throw a IllegalDataException if data (msg + padding) has an invalid size!
     *
     * @return The packet contained in the ONION TUNNEL TRANSPORT payload.
     * @throws InvalidDataException If data (msg + padding) has an invalid size!
     */

    public byte[] getInnerPacket() throws InvalidDataException {
        if(!forMe())
            throw new IllegalStateException("This packet is not supposed for this peer." +
                    " Therefore, the inner packet is just garbage!");
        if(this.payload.length != MAX_INNER_SIZE)
            throw new InvalidDataException("Invalid data length!");
        ByteBuffer buffer = ByteBuffer.wrap(this.payload);
        buffer.order(ByteOrder.BIG_ENDIAN);

        short size = buffer.getShort(); // Data contains another packet -> First two byte are the length
        return Arrays.copyOfRange(this.payload, 0, size);
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = super.serializeBase();
        buffer.put(this.payload);
        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(super.getSizeBase() + this.payload.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_TRANSPORT;
    }
}
