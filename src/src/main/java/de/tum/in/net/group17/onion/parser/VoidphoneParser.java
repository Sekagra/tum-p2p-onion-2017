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
public class VoidphoneParser {
    /**
     * Check an message's size value.
     * This method throws an ParsingException on any errors.
     *
     * @param message The message we shall check.
     */
    protected void checkSize(byte[] message) {
        if (message == null || message.length < 4) // Null or shorter than the header?

            throw new ParsingException("The package must at least contain the header!");

        if(message.length > 65536)
            throw new ParsingException("Packet too long!");

        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.order(ByteOrder.BIG_ENDIAN);
        if((int)buffer.getShort(0) != message.length)
            throw new ParsingException("Package size does not match size field in header!");
    }

    /**
     * Check if the type of given message matches to the expected one.
     * This method will throw a ParsingException on MessageType mismatch.
     *
     * @param message The incoming message.
     * @param expectedType The expected MessageType.
     */
    protected void checkType(byte[] message, MessageType expectedType) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.order(ByteOrder.BIG_ENDIAN);
        MessageType actualType = MessageType.valueOf(buffer.getShort(2));
        if(actualType.getValue() != expectedType.getValue())
            throw new ParsingException("Unexpected message. Have: " + actualType.getValue() +
                    ". Expected: " + expectedType.getValue());
    }

    protected boolean hasMinimumSize(byte[] message) {
        return message.length >= 4;
    }

    /**
     * Get the MessageType of a message.
     *
     * @param message The message we want to read the type from.
     * @return The MessageType.
     */
    protected MessageType extractType(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.order(ByteOrder.BIG_ENDIAN);
        MessageType type = MessageType.valueOf(buffer.getShort(2));
        if(type == null)
            throw new ParsingException("Unknown message type! Have: " + buffer.getShort(2));
        return type;
    }
}
