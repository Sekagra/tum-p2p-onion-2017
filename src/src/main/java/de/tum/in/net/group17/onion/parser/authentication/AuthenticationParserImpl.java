package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;
import de.tum.in.net.group17.onion.parser.MessageType;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class AuthenticationParserImpl extends VoidphoneParser implements AuthenticationParser {
    /**
     * @inheritDoc
     */
    public ParsedMessage buildSessionStart(int requestId, byte[] hostkey) {
        int size = 12 + hostkey.length;
        ASN1Primitive key;

        if(size > 65535)
            throw new ParsingException("Message too large!");

        try {
            key = new ASN1InputStream(new ByteArrayInputStream(hostkey)).readObject().toASN1Primitive();
            return new AuthSessionStartParsedMessage(requestId, key);
        } catch(IOException e) {
            throw new ParsingException("Invalid host key!");
        }
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildSessionIncoming1(int requestId, byte[] hostkey, byte[] payload) {
        int size = 14 + hostkey.length + payload.length;
        ASN1Primitive key;

        if(size > 65535)
            throw new ParsingException("Message too large!");

        try {
            key = new ASN1InputStream(new ByteArrayInputStream(hostkey)).readObject().toASN1Primitive();
            return new AuthSessionIncomingHs1ParsedMessage(requestId, key, payload);
        } catch(IOException e) {
            throw new ParsingException("Invalid host key!");
        }

    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildSessionIncoming2(int requestId, short sessionId, byte[] payload) {
        int size = 12 + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");

        return new AuthSessionIncomingHs2ParsedMessage(sessionId, requestId, payload);
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildLayerEncrypt(int requestId, short[] sessionIds, byte[] payload) {
        checkSizeCryptMessage(requestId, sessionIds, payload);

        return new AuthLayerEncryptParsedMessage(requestId, sessionIds, payload);
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildLayerDecrypt(int requestId, short[] sessionIds, byte[] payload) {
        checkSizeCryptMessage(requestId, sessionIds, payload);

        return new AuthLayerDecryptParsedMessage(requestId, sessionIds, payload);
    }

    private void checkSizeCryptMessage(int requestId, short[] sessionIds, byte[] payload) {
        int size;

        if(sessionIds == null || sessionIds.length > 255 || sessionIds.length < 1)
            throw new ParsingException("Invalid number of session IDs!");

        size = 12 + 2 * sessionIds.length + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildSessionClose(short sessionId) {
        return new AuthSessionCloseParsedMessage(sessionId);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_SESSION_HS1 message.
     *
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity; This method throws a ParsingExecption on every format violations.
     */
    private ParsedMessage parseSessionHandshake1(byte[] message) {
        ByteBuffer buffer;
        byte[] payload = new byte[message.length - 12];
        int requestId;
        short sessId;

        // No explicit length known
        checkType(message, MessageType.AUTH_SESSION_HS1); // Will throw a parsing exception on any error

        buffer = ByteBuffer.wrap(message);
        sessId = buffer.getShort(6);
        requestId = buffer.getInt(8);

        buffer.position(12);
        payload = new byte[message.length - 12];
        buffer.get(payload);

        return new AuthSessionHs1ParsedMessage(sessId, requestId, payload);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_SESSION_HS2 message.
     *
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity; This method throws a ParsingExecption on every format violations.
     */
    private ParsedMessage parseSessionHandshake2(byte[] message) {
        ByteBuffer buffer;
        byte[] payload = new byte[message.length - 12];
        int requestId;
        short sessId;

        // No explicit length known
        checkType(message, MessageType.AUTH_SESSION_HS2); // Will throw a parsing exception on any error

        buffer = ByteBuffer.wrap(message);
        sessId = buffer.getShort(6);
        requestId = buffer.getInt(8);

        buffer.position(12);
        buffer.get(payload);

        return new AuthSessionHs2ParsedMessage(sessId, requestId, payload);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_LAYER_ENCRYPT_RESP message.
     *
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity; This method throws a ParsingExecption on every format violations.
     */
    private ParsedMessage parseLayerEncryptResponse(byte[] message) {
        return parseLayerCryptResponse(MessageType.AUTH_LAYER_ENCRYPT_RESP, message);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_LAYER_DECRYPT_RESP message.
     *
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity; This method throws a ParsingExecption on every format violations.
     */
    private ParsedMessage parseLayerDecryptResponse(byte[] message) {
        return parseLayerCryptResponse(MessageType.AUTH_LAYER_DECRYPT_RESP, message);
    }

    /**
     * Parse a AUTH_LAYER_EN-/DECRYPT_RESP message.
     *
     * @param type Is this a decrypt/encrypt message?
     * @param message The received message.
     * @return The parsed message to confirm its validity; This method throws a ParsingException on every format violations.
     */
    private ParsedMessage parseLayerCryptResponse(MessageType type, byte[] message) {
        ByteBuffer buffer;
        byte[] payload;
        int requestId;

        checkType(message, type); // Will throw a parsing exception on any error

        buffer = ByteBuffer.wrap(message);
        requestId = buffer.getInt(8);
        payload = new byte[message.length - 12];
        buffer.position(12);
        buffer.get(payload);

        return (type == MessageType.AUTH_LAYER_ENCRYPT_RESP ?
                new AuthLayerEncryptResParsedMessage(requestId, payload) :
                new AuthLayerDecryptResParsedMessage(requestId, payload));
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage parse(byte[] message) {
        checkSize(message); // Throws an exception if an error occurs

        switch (extractType(message)) {
            case AUTH_SESSION_HS1:
                return parseSessionHandshake1(message);
            case AUTH_SESSION_HS2:
                return parseSessionHandshake2(message);
            case AUTH_LAYER_ENCRYPT_RESP:
                return parseLayerEncryptResponse(message);
            case AUTH_LAYER_DECRYPT_RESP:
                 return parseLayerDecryptResponse(message);
            default:
                throw new ParsingException("Not able to parse message. Type: " + extractType(message).getValue() + "!");
        }
    }
}
