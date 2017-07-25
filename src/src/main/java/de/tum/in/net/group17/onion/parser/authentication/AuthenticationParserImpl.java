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

/**
 * Created by Christoph Rudolf on 25.05.17.
 *
 * Marko Dorfhuber (PraMiD) 24.07.2017: Added Cipher Messages
 */
public class AuthenticationParserImpl extends VoidphoneParser implements AuthenticationParser {
    /**
     * @inheritDoc
     */
    public ParsedMessage buildSessionStart(int requestId, byte[] hostkey) throws ParsingException {
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
    public ParsedMessage buildSessionIncoming1(int requestId, byte[] payload) throws ParsingException {
        if(14 + payload.length > 65536)
            throw new ParsingException("Message too large!");

        return new AuthSessionIncomingHs1ParsedMessage(requestId, payload);
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildSessionIncoming2(int requestId, short sessionId, byte[] payload) throws ParsingException {
        int size = 12 + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");

        return new AuthSessionIncomingHs2ParsedMessage(sessionId, requestId, payload);
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildLayerEncrypt(int requestId, short[] sessionIds, byte[] payload) throws ParsingException {
        checkSizeCryptMessage(requestId, sessionIds, payload);

        return new AuthLayerEncryptParsedMessage(requestId, sessionIds, payload);
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildLayerDecrypt(int requestId, short[] sessionIds, byte[] payload) throws ParsingException {
        checkSizeCryptMessage(requestId, sessionIds, payload);

        return new AuthLayerDecryptParsedMessage(requestId, sessionIds, payload);
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildSessionClose(short sessionId) throws ParsingException {
        return new AuthSessionCloseParsedMessage(sessionId);
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildCipherEncrypt(boolean stillEncrypted, int requestId, byte[] payload) {
        return buildCipherCryptMessage(MessageType.AUTH_CIPHER_ENCRYPT, stillEncrypted, requestId, payload);
    }

    /**
     * @inheritDoc
     */
    public ParsedMessage buildCipherDecrypt(boolean stillEncrypted, int requestId, byte[] payload) throws ParsingException {
        return buildCipherCryptMessage(MessageType.AUTH_CIPHER_DECRYPT, stillEncrypted, requestId, payload);
    }

    /**
     * Create a new AUTH_CIPHER_EN-/DECRYPT message with the given parameters.
     * This method throws a ParsingException if an error occurs.
     *
     * @param type Either an AUTH_CIPHER_ENCRYPT or an AUTH_CIPHER_DECRYPT message.
     * @param stillEncrypted Flag that indicates if the message is encrypted the first time (encryption mode)
     *                       or is still encrypted after removing the last layer of encryption (decryption mode)
     * @param requestId The request ID that shall be used.
     * @param payload The payload that shall be en-/decrypted.
     *
     * @return Either an AuthCipherEncryptParsedMessage or an AuthCipherDecryptParsedMessage after checking
     *          all parameters.
     */
    private ParsedMessage buildCipherCryptMessage(MessageType type, boolean stillEncrypted, int requestId, byte[] payload)
    {
        if(payload == null || payload.length < 1)
            throw new ParsingException("Illegal payload for encryption/decryption!");

        // TODO: Change the size in all classes to match the ONION message size
        if(12 + payload.length > 65536)
            throw new ParsingException("Payload too long to build an AUTH CIPHER message!");

        if(type == MessageType.AUTH_CIPHER_ENCRYPT)
            return new AuthCipherEncryptParsedMessage(stillEncrypted, requestId, payload);
        else // Private message -> Do not check if type is invalid
            return new AuthCipherDecryptParsedMessage(stillEncrypted, requestId, payload);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_SESSION_HS1 message.
     *
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity; This method throws a ParsingExecption on every format violations.
     */
    private ParsedMessage parseSessionHandshake1(byte[] message) throws ParsingException {
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
     * @return The parsed message to confirm its validity; This method throws a ParsingExecption on every format violation.
     */
    private ParsedMessage parseSessionHandshake2(byte[] message) throws ParsingException {
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
     * Parse a AUTH_LAYER_EN-/DECRYPT_RESP message.
     *
     * @param type Is this a decrypt/encrypt message?
     * @param message The received message.
     * @return The parsed message to confirm its validity; This method throws a ParsingException on every format violation.
     */
    private ParsedMessage parseLayerCryptResponse(MessageType type, byte[] message) throws ParsingException {
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
     * Parse an ONION_AUTH_ERROR message.
     *
     * @param message The message sent by the Onion Auth module.
     * @return A ParsedMessage to confirm the validity of the message; This method throws a ParsingException on every format violation.
     */
    private ParsedMessage parseAuthErrorRespone(byte[] message) throws ParsingException {
        ByteBuffer buffer;

        checkType(message, MessageType.AUTH_ERROR); // Will throw an parsing exception on every error

        buffer = ByteBuffer.wrap(message);
        return new AuthErrorParsedMessage(buffer.getInt(8));
    }

    /**
     * @inheritDoc
     */
    @Override
    public ParsedMessage parseMsg(byte[] message) throws ParsingException {
        checkSize(message); // Throws an exception if an error occurs

        switch (extractType(message)) {
            case AUTH_SESSION_HS1:
                return parseSessionHandshake1(message);
            case AUTH_SESSION_HS2:
                return parseSessionHandshake2(message);
            case AUTH_LAYER_ENCRYPT_RESP:
                return parseLayerCryptResponse(MessageType.AUTH_LAYER_ENCRYPT_RESP, message);
            case AUTH_LAYER_DECRYPT_RESP:
                return parseLayerCryptResponse(MessageType.AUTH_LAYER_DECRYPT_RESP, message);
            case AUTH_CIPHER_ENCRYPT_RESP:
                return parseLayerCryptResponse(MessageType.AUTH_CIPHER_ENCRYPT_RESP, message);
            case AUTH_CIPHER_DECRYPT_RESP:
                return parseLayerCryptResponse(MessageType.AUTH_CIPHER_DECRYPT_RESP, message);
            case AUTH_ERROR:
                return parseAuthErrorRespone(message);
            default:
                throw new ParsingException("Not able to parse message. Type: " + extractType(message).getValue() + "!");
        }
    }

    private void checkSizeCryptMessage(int requestId, short[] sessionIds, byte[] payload) {
        int size;

        if(sessionIds == null || sessionIds.length > 255 || sessionIds.length < 1)
            throw new ParsingException("Invalid number of session IDs!");

        size = 12 + 2 * sessionIds.length + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");
    }
}
