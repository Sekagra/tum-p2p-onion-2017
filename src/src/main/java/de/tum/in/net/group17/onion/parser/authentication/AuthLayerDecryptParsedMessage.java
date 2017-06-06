package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public class AuthLayerDecryptParsedMessage extends AuthLayerCryptParsedMessage {
    /**
     * Create a new AUTH LAYER DECRYPT message.
     * This object may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId  The request ID of this message.
     * @param sessionIds An array of session IDs used for encryption.
     * @param payload    The payload that shall be encrypted.
     */
    protected AuthLayerDecryptParsedMessage(int requestId, short[] sessionIds, byte[] payload) {
        super(requestId, sessionIds, payload);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.AUTH_LAYER_DECRYPT;
    }
}
