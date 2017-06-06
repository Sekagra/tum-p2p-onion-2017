package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public abstract class AuthLayerCryptParsedMessage extends ParsedMessage {
    private int requestId;
    private short[] sessIds;
    private byte[] payload;

    /**
     * Create a new AUTH LAYER EN-/DECRYPT message.
     * This object may only be created by an AuthenticationParser after checking all parameters.
     *
     * @param requestId The request ID of this message.
     * @param sessionIds An array of session IDs used for encryption.
     * @param payload The payload that shall be encrypted.
     */
    protected AuthLayerCryptParsedMessage(int requestId, short[] sessionIds, byte[] payload) {
        if(sessionIds == null || sessionIds.length < 1)
            throw new IllegalArgumentException("This message must contain at least one session ID!");
        if(sessionIds.length > 255)
            throw new IllegalArgumentException("We can only handle up to 255 session IDs!");
        this.requestId = requestId;
        this.sessIds = sessionIds;
        this.payload = payload;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putShort((short)0);
        buffer.put((byte)sessIds.length);
        buffer.put((byte)0);
        buffer.putInt(requestId);
        for(short id : sessIds) {
            buffer.putShort(id);
        }
        buffer.put(payload);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(12 + sessIds.length * 2 + payload.length);
    }

    /**
     * Get the number of session IDs sent with this message.
     *
     * @return The number of sent session IDs.
     */
    public byte getNoLayers() {
        return (byte)(sessIds.length);
    }

    /**
     * Get all session IDs sent in this message.
     *
     * @return A short[] containing all used session IDs.
     */
    public short[] getSessionIds() {
        return sessIds;
    }

    /**
     * Get the session ID with the given index.
     *
     * @param index Specifies which session ID the user wants to retrieve.
     * @return The specified session ID.
     */
    public short getSessionId(int index) {
        if(index >= sessIds.length)
            throw new ArrayIndexOutOfBoundsException();
        return sessIds[index];
    }

    /**
     * Get the request ID of this message.
     *
     * @return This message's request ID.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Get the payload contained in this message.
     *
     * @return A byte[] containing the payload.
     */
    public byte[] getPayload() {
        return payload;
    }
}
