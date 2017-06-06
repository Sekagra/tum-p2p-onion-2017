package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public class AuthSessionCloseParsedMessage extends ParsedMessage {
    private short sessId;

    /**
     * Create a new AUTH SESSION CLOSE message with the given parameters.
     * This object may only be created by an AuthenticationParser after checking the parameters.
     *
     * @param sessionId The ID of the session that shall be closed.
     */
    protected AuthSessionCloseParsedMessage(short sessionId) {
        this.sessId = sessionId;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.putShort((short)00);
        buffer.putShort(sessId);
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
        return MessageType.AUTH_SESSION_CLOSE;
    }

    /**
     * Get the ID of the corresponding session.
     *
     * @return The corresponding session ID.
     */
    public short getSessionId() {
        return sessId;
    }
}
