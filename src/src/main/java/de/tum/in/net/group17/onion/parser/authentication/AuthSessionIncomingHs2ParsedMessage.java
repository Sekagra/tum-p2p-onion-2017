package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public class AuthSessionIncomingHs2ParsedMessage extends AuthParsedMessage {
    private short sessId;
    private byte[] payload;

    /**
     * Create a new AUTH SESSION INCOMING HS2 message.
     * This object may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param sessionId The ID of the current session.
     * @param requestId The ID of this request.
     * @param payload The payload sent by the ONION AUTH module.
     */
    protected AuthSessionIncomingHs2ParsedMessage(short sessionId, int requestId, byte[] payload) {
        super(requestId);
        this.sessId = sessionId;
        this.payload = payload;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putShort((short)0);
        buffer.putShort(sessId);
        buffer.putInt(requestId);
        buffer.put(payload);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(12 + payload.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.AUTH_SESSION_INCOMING_HS2;
    }

    /**
     * Get the current session ID.
     *
     * @return The ID of the session this message belongs to.
     */
    public short getSessionId() {
        return sessId;
    }

    /**
     * Get the request ID.
     *
     * @return The ID of the current request.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Get the payload sent in this message.
     *
     * @return A byte[] containing this message's payload.
     */
    public byte[] getPayload() {
        return payload;
    }
}
