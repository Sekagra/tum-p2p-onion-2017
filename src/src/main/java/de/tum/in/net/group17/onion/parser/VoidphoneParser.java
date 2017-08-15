package de.tum.in.net.group17.onion.parser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation that provides common methods used among all parser
 * implementations for the current Voidphone packet format.
 *
 * Created by Christoph Rudolf on 25.05.17.
 * Changes by Marko Dorfhuber(PraMiD) on 27.05.17: Added checks to checkSize
 */
public abstract class VoidphoneParser {
    /**
     * Check an message's size value.
     *
     *
     * @param message The message we shall check.
     *
     * @throws ParsingException If the size in the packet is invalid.
     */
    protected void checkSize(byte[] message) throws ParsingException {
        if (message == null || message.length < 4) // Null or shorter than the header?
            throw new ParsingException("The package must at least contain the header!");

        if(message.length > 65536)
            throw new ParsingException("Packet too long!");

        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.order(ByteOrder.BIG_ENDIAN);
        if((int)buffer.getShort(0) != message.length)
            throw new ParsingException("Packet size does not match size field in header! Size of array: "
                    + message.length + "; Size in packet: " + (int)buffer.getShort(0));
    }

    /**
     * Check if the type of given message matches to the expected one.
     * This method will throw a ParsingException on MessageType mismatch.
     *
     * @param message The incoming message.
     * @param expectedType The expected MessageType.
     *
     * @throws ParsingException If the incoming data has another type than the expected one.
     */
    protected void checkType(byte[] message, MessageType expectedType) throws ParsingException {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.order(ByteOrder.BIG_ENDIAN);
        MessageType actualType = MessageType.valueOf(buffer.getShort(2));
        if(actualType.getValue() != expectedType.getValue())
            throw new ParsingException("Unexpected message. Have: " + actualType.getValue() +
                    ". Expected: " + expectedType.getValue());
    }

    /**
     * Get the MessageType of a message.
     *
     * @param message The message we want to read the type from.
     * @return The MessageType.
     *
     * @throws ParsingException Get the type of the incoming message.
     */
    protected MessageType extractType(byte[] message) throws ParsingException {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.order(ByteOrder.BIG_ENDIAN);
        MessageType type = MessageType.valueOf(buffer.getShort(2));
        if(type == null)
            throw new ParsingException("Unknown message type! Have: " + buffer.getShort(2));
        return type;
    }
}
