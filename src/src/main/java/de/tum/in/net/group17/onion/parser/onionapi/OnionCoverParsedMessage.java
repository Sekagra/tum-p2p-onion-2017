package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 04.06.17.
 */
public class OnionCoverParsedMessage extends ParsedMessage {
    private final short coverSize;

    /**
     * Create a ONION COVER message with the given parameters.
     * This object may only be created by an OnionParser after checking parameters!
     *
     * @param size Size of the cover traffic.
     */
    public OnionCoverParsedMessage(short size) {
        this.coverSize = size;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.putShort(coverSize);
        buffer.putShort((short)0); // Reserved area

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return 8;
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_COVER;
    }
}
