package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.07.17.
 *
 * Represents an AUTH CIPHER ENCRYPT RESP message.
 * Objects of this class may only be created by an AuthenticationParsed after checking all parameters.
 */
public class AuthCipherEncryptResParsedMessage extends AuthCryptResParsedMessage {
    /**
     * Create a new AUTH CIPHER ENCRYPT RESP message.
     * Objects of this class may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The ID used in the encryption request.
     * @param payload The encrypted payload.
     */
    AuthCipherEncryptResParsedMessage(int requestId, byte[] payload) {
        super(requestId, payload);
    }

    @Override
    protected int getFlagsArea() {
        return 0;
    }

    /**
     * @inheritDoc
     */
    @Override
    public MessageType getType() {
        return MessageType.AUTH_CIPHER_ENCRYPT_RESP;
    }
}
