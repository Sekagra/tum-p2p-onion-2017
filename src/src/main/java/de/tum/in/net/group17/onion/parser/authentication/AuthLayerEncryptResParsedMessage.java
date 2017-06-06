package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public class AuthLayerEncryptResParsedMessage extends AuthLayerCryptResParsedMessage {
    /**
     * Create a new AUTH LAYER ENCRYPT RESP message.
     * This object may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The used request ID.
     * @param payload   The encrypted payload.
     */
    protected AuthLayerEncryptResParsedMessage(int requestId, byte[] payload) {
        super(requestId, payload);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.AUTH_LAYER_ENCRYPT_RESP;
    }
}
