package de.tum.in.net.group17.onion.parser.authentication;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public abstract class AuthCryptResParsedMessage extends AuthParsedMessage {
    private byte[] payload;

    /**
     * Create a new AUTH LAYER EN-\DECRYPT RESP message.
     * This object may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The used request ID.
     * @param payload The encrypted payload.
     */
    protected AuthCryptResParsedMessage(int requestId, byte[] payload) {
        super(requestId);
        this.payload = payload;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putInt(getFlagsArea());
        buffer.putInt(requestId);
        buffer.put(payload);

        return buffer.array();
    }

    /**
     * Get the bits we have to set in the flags area.
     * (AUTH CIPHER DECRYPT uses one bit)
     *
     * @return The flags area of this message type.
     */
    protected abstract int getFlagsArea();

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(12 + payload.length);
    }

    /**
     * Get the request ID of this message.
     *
     * @return The message's request ID.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Get the encrypted payload.
     *
     * @return A byte[] containing the en-/decrypted payload.
     */
    public byte[] getPayload() {
        return payload;
    }
}
