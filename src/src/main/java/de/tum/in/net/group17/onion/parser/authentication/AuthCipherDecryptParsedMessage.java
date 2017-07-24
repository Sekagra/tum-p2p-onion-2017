package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.07.17.
 *
 * Create a new AUTH CIPHER DECRYPT message.
 * Objects of this class may only be created by an AuthenticationParser after checking all parameters.
 */
public class AuthCipherDecryptParsedMessage extends AuthCipherCryptParsedMessage {
    /**
     * @inheritDoc
     */
    public AuthCipherDecryptParsedMessage(boolean stillEncrypted, int requestId, byte[] payload)
    {
        super(stillEncrypted, requestId, payload);
    }

    /**
     * @inheritDoc
     */
    @Override
    public MessageType getType() {
        return MessageType.AUTH_CIPHER_DECRYPT;
    }
}
