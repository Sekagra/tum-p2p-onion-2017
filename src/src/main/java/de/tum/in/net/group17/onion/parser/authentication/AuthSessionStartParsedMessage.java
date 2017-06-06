package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 06.06.17.
 */
public class AuthSessionStartParsedMessage extends ParsedMessage {
    private int requestId;
    private ASN1Primitive key;

    /**
     * Create a new AUTH SESSION START message with the given parameters.
     * This message may only be created by an AuthenticationParser after checking the parameters.
     *
     * @param requestId The request identifier of this message.
     * @param key The key used in this session.
     */
    protected AuthSessionStartParsedMessage(int requestId, ASN1Primitive key) {
        this.requestId = requestId;
        this.key = key;
    }


    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();

        buffer.putInt(0);
        buffer.putInt(requestId);
        buffer.put(getRawKey());

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(12 + getRawKey().length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.AUTH_SESSION_START;
    }

    /**
     * Get the used request ID.
     *
     * @return The request ID used in this message.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Get the used host key.
     *
     * @return A ASN1Primitive containing the host key used in this message.
     */
    public ASN1Primitive getKey() {
        return key;
    }

    /**
     * Get the encoded host key.
     *
     * @return A byte[] containing the host key.
     */
    private byte[] getRawKey() {
        try {
            return key.getEncoded();
        } catch(IOException e) {
            // Should be checked beforehand!
            throw new Error("Invalid key");
        }
    }
}
