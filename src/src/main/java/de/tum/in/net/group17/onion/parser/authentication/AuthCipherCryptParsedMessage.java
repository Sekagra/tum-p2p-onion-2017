package de.tum.in.net.group17.onion.parser.authentication;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.07.17.
 *
 * Represents an AUTH CIPHER EN-/DECRYPT RESP message.
 * Objects of this class may only be created by an AuthenticationParsed after checking all parameters.
 */
public abstract class AuthCipherCryptParsedMessage extends AuthParsedMessage {
    private final byte[] payload;
    private final boolean stillEnc;


    /**
     * Create a new AUTH CIPHER EN-/DECRYPT message.
     * Objects of this class may only be created by an AuthenticationParsed after checking all parameters.
     *
     * @param stillEncrypted Flag that indicates if the payload is encrypted the first time (encrypt) or remains
     *                       encrypted after the next layer of is removed (decryption).
     * @param requestId The request ID used for this en-/decryption request to the AUTH module.
     * @param payload The payload to en-/decrypt.
     */
    public AuthCipherCryptParsedMessage(boolean stillEncrypted, int requestId, byte[] payload)
    {
        super(requestId);
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
        buffer.put(payload);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    @Override
    public short getSize() {
        return (short)(12 + payload.length);
    }
}
