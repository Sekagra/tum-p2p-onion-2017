package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 21.06.17.
 */
public class AuthErrorParsedMessage extends ParsedMessage {
    private final int requestID;

    /**
     * Create a new AuthErrorParsedMessage containing the given request ID.
     * This message may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestID The ID used in the corresponding request.
     */
    public AuthErrorParsedMessage(int requestID) {
        this.requestID = requestID;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putInt(4, 0); // Reserved
        buffer.putInt(8, requestID);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return 12;
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.AUTH_ERROR;
    }

    /**
     * Get the request ID contained in this message.
     *
     * @return The contained request ID.
     */
    public int getRequestID() {
        return requestID;
    }
}
