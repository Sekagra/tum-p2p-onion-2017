package de.tum.in.net.group17.onion.parser.authentication;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.07.17.
 *
 * Represents an AUTH CIPHER EN-/DECRYPT RESP message.
 * Objects of this class may only be created by an AuthenticationParsed after checking all parameters.
 */
public abstract class AuthCipherCryptParsedMessage extends AuthParsedMessage {
    private short sessionId;
    private final byte[] payload;

    // This flag is ignored in the case of an AUTH CIPHER DECRYPT message
    private final boolean stillEnc;


    /**
     * Create a new AUTH CIPHER EN-/DECRYPT message.
     * Objects of this class may only be created by an AuthenticationParsed after checking all parameters.
     *
     * @param stillEncrypted Flag that indicates if the payload is encrypted the first time (encrypt) or remains
     *                       encrypted after the next layer of is removed (decryption).
     * @param requestId The request ID used for this en-/decryption request to the AUTH module.
     * @param sessionId The session ID used for en-/decryption.
     * @param payload The payload to en-/decrypt.
     */
    public AuthCipherCryptParsedMessage(boolean stillEncrypted, int requestId, short sessionId, byte[] payload)
    {
        super(requestId);
        this.sessionId = sessionId;
        this.stillEnc = stillEncrypted;
        this.payload = payload;
    }

    /**
     * @inheritDoc
     */
    @Override
    public byte[] serialize() {
        int bits = 0;
        ByteBuffer buffer = buildHeader();

        if(stillEnc)
            bits |= 0x00000001;
        buffer.putInt(bits);
        buffer.putInt(requestId);
        buffer.putShort(sessionId);

        buffer.put(payload);

        return buffer.array();
    }

    /**
     * Return the session ID used for en-/decryption.
     *
     * @return The used session ID.
     */
    public short getSessionId() {
        return sessionId;
    }

    /**
     * Get the en-/decrypted payload.
     *
     * @return A byte[] containing the payload.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Flag determines if the payload is already encrypted (encrypt message) or is still encrypted after decryption
     * (decrypt message).
     *
     * @return True if the payload is encrypted.
     */
    public boolean isStillEnc() {
        return stillEnc;
    }

    /**
     * @inheritDoc
     */
    @Override
    public short getSize() {
        return (short)(14 + payload.length);
    }   // Head (4) + Bitmask (4) + Request ID (4) + Session ID (2)
}
