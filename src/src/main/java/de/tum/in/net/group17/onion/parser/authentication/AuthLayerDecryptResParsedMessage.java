package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public class AuthLayerDecryptResParsedMessage extends AuthLayerCryptResParsedMessage {
    /**
     * Create a new AUTH LAYER DECRYPT RESP message.
     * This object may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The used request ID.
     * @param payload   The encrypted payload.
     */
    protected AuthLayerDecryptResParsedMessage(int requestId, byte[] payload) {
        super(requestId, payload);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.AUTH_LAYER_DECRYPT_RESP;
    }
}
