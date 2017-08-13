package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 21.06.17.
 */
public class AuthErrorParsedMessage extends AuthParsedMessage {
    /**
     * Create a new AuthErrorParsedMessage containing the given request ID.
     * This message may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The ID used in the corresponding request.
     */
    public AuthErrorParsedMessage(int requestId) {
        super(requestId);
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putInt(4, 0); // Reserved
        buffer.putInt(8, requestId);

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
}
