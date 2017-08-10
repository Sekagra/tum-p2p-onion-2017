package de.tum.in.net.group17.onion.parser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Marko Dorfhuber(PraMiD) on 04.06.17.
 */
public abstract class ParsedMessage {
    /**
     * Serialize this parsed message.
     *
     * @return byte[] containing the serialized message.
     */
    public abstract byte[] serialize();

    /**
     * Get the size of this message.
     *
     * @return The message size.
     */
    public abstract short getSize();

    /**
     * Get the message's type.
     *
     * @return MessageType of this message.
     */
    public abstract MessageType getType();

    /**
     * Create a ByteBuffer containing the header of this message.
     * The byte order of the buffer is big endian.
     *
     * @return A ByteBuffer containing the message header.
     */
    protected ByteBuffer buildHeader() {
        short size = getSize();
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putShort(size);
        buffer.putShort(getType().getValue());

        return buffer;
    }
}
