package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public abstract class AuthLayerCryptResParsedMessage extends ParsedMessage {
    private int requestId;
    private byte[] payload;

    /**
     * Create a new AUTH LAYER EN-\DECRYPT RESP message.
     * This object may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The used request ID.
     * @param payload The encrypted payload.
     */
    protected AuthLayerCryptResParsedMessage(int requestId, byte[] payload) {
        this.requestId = requestId;
        this.payload = payload;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putInt(0);
        buffer.putInt(requestId);
        buffer.put(payload);

        return buffer.array();
    }

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
