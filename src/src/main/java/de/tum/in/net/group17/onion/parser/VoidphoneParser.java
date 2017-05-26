package de.tum.in.net.group17.onion.parser;

import java.nio.ByteBuffer;

/**
 * Implementation that provides common methods used among all parser
 * implementations for the current Voidphone packet format.
 * Created by Christoph Rudolf on 25.05.17.
 */
public class VoidphoneParser {
    protected boolean checkSize(byte[] message) {
        //extract first 2 bytes as int and check the size
        ByteBuffer buffer = ByteBuffer.wrap(message);
        return buffer.getShort() == message.length;
    }

    protected boolean checkType(byte[] message, MessageType exptectedType) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.position(2);
        return buffer.getShort() == exptectedType.getValue();
    }

    protected boolean hasMinimumSize(byte[] message) {
        return message.length >= 4;
    }

    protected MessageType extractType(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.position(2);
        return MessageType.values()[buffer.getShort()];
    }

    /**
     * Creates a parsed message on behalf of the inheriting parsers in order to set the constructor of ParsedMessage to
     * a lower visibility.
     * @param message The raw message data.
     * @return A ParsedMessage
     */
    protected ParsedMessage createParsedMessage(byte[] message) {
        return new ParsedMessage(message);
    }
}
