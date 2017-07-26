package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public class AuthSessionIncomingHs1ParsedMessage extends AuthParsedMessage {
    private byte[] payload;

    /**
     * Create a new AUTH SESSION INCOMING HS1 message.
     * This message may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The request ID of this message.
     * @param payload The payload sent to the ONION AUTH module
     */
    protected AuthSessionIncomingHs1ParsedMessage(int requestId, byte[] payload) {
        super(requestId);
        this.payload = payload;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putInt(0);
        buffer.putInt(requestId);
        buffer.put(payload);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(14 + payload.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.AUTH_SESSION_INCOMING_HS1;
    }

    /**
     * Get the request ID of this message.
     *
     * @return The used request ID.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Return the message's payload.
     *
     * @return A byte[] containing the payload.
     */
    public byte[] getPayload() {
        return payload;
    }
}
