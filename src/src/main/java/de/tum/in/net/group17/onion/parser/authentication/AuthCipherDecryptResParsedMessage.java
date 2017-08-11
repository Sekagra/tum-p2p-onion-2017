package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.07.17.
 *
 * Represents an AUTH CIPHER DECRYPT RESP message.
 * Objects of this class may only be created by an AuthenticationParsed after checking all parameters.
 */
public class AuthCipherDecryptResParsedMessage extends AuthCryptResParsedMessage {
    boolean stillEncrypted;

    /**
     * Create a new AUTH CIPHER DECRYPT RESP message.
     * Objects of this class may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param stillEncrypted Determine if the contained payload is still encrypted or not.
     * @param requestId The ID used in the decryption request.
     * @param payload The decrypted payload.
     */
    AuthCipherDecryptResParsedMessage(boolean stillEncrypted, int requestId, byte[] payload) {
        super(requestId, payload);
        this.stillEncrypted = stillEncrypted;
    }

    /**
     * Determine if the payload is still encrypted.
     *
     * @return True if the payload is encrypted.
     */
    public boolean isStillEncrypted() {
        return this.stillEncrypted;
    }

    /**
     * @inheritDoc
     */
    @Override
    public MessageType getType() {
        return MessageType.AUTH_CIPHER_DECRYPT_RESP;
    }

    @Override
    protected int getFlagsArea() {
        return stillEncrypted ? 0x00000001 : 0x00000000;
    }
}
