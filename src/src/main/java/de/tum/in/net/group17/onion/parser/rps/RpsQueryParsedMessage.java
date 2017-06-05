package de.tum.in.net.group17.onion.parser.rps;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Marko Dorfhuber(PraMiD) on 04.06.17.
 */
public class RpsQueryParsedMessage extends ParsedMessage {
    /**
     * Create a new RPS QUERY message.
     * This object may only be created from a corresponding RPSParser.
     */
    protected RpsQueryParsedMessage() { }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        return buildHeader().array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return 4;
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.RPS_QUERY;
    }
}
