package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.07.17.
 *
 * Represents an AUTH CIPHER DECRYPT RESP message.
 * Objects of this class may only be created by an AuthenticationParsed after checking all parameters.
 */
public class AuthCipherDecryptResParsedMessage extends AuthLayerCryptResParsedMessage {
    /**
     * Create a new AUTH CIPHER DECRYPT RESP message.
     * Objects of this class may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The ID used in the decryption request.
     * @param payload The decrypted payload.
     */
    AuthCipherDecryptResParsedMessage(int requestId, byte[] payload) {
        super(requestId, payload);
    }

    /**
     * @inheritDoc
     */
    @Override
    public MessageType getType() {
        return MessageType.AUTH_CIPHER_DECRYPT_RESP;
    }
}
