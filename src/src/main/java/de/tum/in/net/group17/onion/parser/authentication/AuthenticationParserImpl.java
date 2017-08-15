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
 *
 * A parser for all AUTH messages for the Specification (v4.0) of the VoidPhone application.
 */
public class AuthenticationParserImpl extends VoidphoneParser implements AuthenticationParser {
    /**
     * @inheritDoc
     */
    @Override
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
    @Override
    public ParsedMessage buildSessionIncoming1(int requestId, byte[] payload) throws ParsingException {
        if(12 + payload.length > 65536)
            throw new ParsingException("Message too large!");

        return new AuthSessionIncomingHs1ParsedMessage(requestId, payload);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ParsedMessage buildSessionIncoming2(int requestId, short sessionId, byte[] payload) throws ParsingException {
        int size = 12 + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");

        return new AuthSessionIncomingHs2ParsedMessage(sessionId, requestId, payload);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ParsedMessage buildLayerEncrypt(int requestId, short[] sessionIds, byte[] payload) throws ParsingException {
        checkSizeCryptMessage(sessionIds, payload);

        return new AuthLayerEncryptParsedMessage(requestId, sessionIds, payload);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ParsedMessage buildLayerDecrypt(int requestId, short[] sessionIds, byte[] payload) throws ParsingException {
        checkSizeCryptMessage(sessionIds, payload);

        return new AuthLayerDecryptParsedMessage(requestId, sessionIds, payload);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ParsedMessage buildSessionClose(short sessionId) throws ParsingException {
        return new AuthSessionCloseParsedMessage(sessionId);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ParsedMessage buildCipherEncrypt(boolean stillEncrypted, int requestId, short sessionId, byte[] payload) throws ParsingException {
        if(payload == null || payload.length < 1)
            throw new ParsingException("Illegal payload for encryption/decryption!");

        if(12 + payload.length > 65536)
            throw new ParsingException("Payload too long to build an AUTH CIPHER message!");

        return new AuthCipherEncryptParsedMessage(stillEncrypted, requestId, sessionId, payload);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ParsedMessage buildCipherDecrypt(int requestId, short sessionId, byte[] payload) throws ParsingException {
        if(payload == null || payload.length < 1)
            throw new ParsingException("Illegal payload for encryption/decryption!");

        if(12 + payload.length > 65536)
            throw new ParsingException("Payload too long to build an AUTH CIPHER message!");

        return new AuthCipherDecryptParsedMessage(requestId, sessionId, payload);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH SESSION HS1 message.
     *
     *
     * @param message The raw message as byte[].
     *
     * @return The parsed message to confirm its validity.
     *
     * @throws ParsingException If the input does not contain an AUTH SESSION HS1 message.
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
     * Confirm the size and type matching the specification of the AUTH SESSION HS2 message.
     *
     *
     * @param message The raw message as byte[].
     *
     * @return The parsed message to confirm its validity.
     *
     * @throws ParsingException If the input does not contain an AUTH SESSION HS2 message.
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
     * Parse a AUTH [LAYER|CIPHER] [EN-|DECRYPT] RESP message.
     *
     *
     * @param type Is this a decrypt/encrypt message?
     * @param message The received message.
     *
     * @return The parsed message to confirm its validity.
     *
     * @throws ParsingException If the input does not contain a message of the type given by the type parameter.
     */
    private ParsedMessage parseCryptResponse(MessageType type, byte[] message) throws ParsingException {
        ByteBuffer buffer;
        int flagsArea;
        int requestId;
        byte[] payload;

        checkType(message, type); // Will throw a parsing exception on any error

        buffer = ByteBuffer.wrap(message);
        flagsArea = buffer.getInt(4);
        requestId = buffer.getInt(8);
        payload = new byte[message.length - 12];
        buffer.position(12);
        buffer.get(payload);

        switch (type) {
            case AUTH_LAYER_ENCRYPT_RESP:
                return new AuthLayerEncryptResParsedMessage(requestId, payload);
            case AUTH_LAYER_DECRYPT_RESP:
                return new AuthLayerDecryptResParsedMessage(requestId, payload);
            case AUTH_CIPHER_ENCRYPT_RESP:
                return new AuthCipherEncryptResParsedMessage(requestId, payload);
            case AUTH_CIPHER_DECRYPT_RESP:
                return new AuthCipherDecryptResParsedMessage((flagsArea & 0x00000001) != 0, requestId, payload);
        }

        return (type == MessageType.AUTH_LAYER_ENCRYPT_RESP ?
                new AuthLayerEncryptResParsedMessage(requestId, payload) :
                new AuthLayerDecryptResParsedMessage(requestId, payload));
    }

    /**
     * Parse an ONION AUTH ERROR message.
     *
     * @param message The message sent by the Onion Auth module.
     *
     * @return A ParsedMessage to confirm the validity of the message.
     *
     * @throws ParsingException If the input does not contain an AUTH ERROR message.
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
                return parseCryptResponse(MessageType.AUTH_LAYER_ENCRYPT_RESP, message);
            case AUTH_LAYER_DECRYPT_RESP:
                return parseCryptResponse(MessageType.AUTH_LAYER_DECRYPT_RESP, message);
            case AUTH_CIPHER_ENCRYPT_RESP:
                return parseCryptResponse(MessageType.AUTH_CIPHER_ENCRYPT_RESP, message);
            case AUTH_CIPHER_DECRYPT_RESP:
                return parseCryptResponse(MessageType.AUTH_CIPHER_DECRYPT_RESP, message);
            case AUTH_ERROR:
                return parseAuthErrorRespone(message);
            default:
                throw new ParsingException("Not able to parse message. Type: " + extractType(message).getValue() + "!");
        }
    }

    /**
     * Check if the packet with all parameters encoded would exceed the packet size limit given by the specification
     * (65536 byte).
     *
     *
     * @param sessionIds The session IDs we would add to the packet.
     * @param payload The payload we would add to the packet.
     *
     * @throws ParsingException If the message size is too large.
     */
    private void checkSizeCryptMessage(short[] sessionIds, byte[] payload) throws ParsingException {
        int size;

        if(sessionIds == null || sessionIds.length > 255 || sessionIds.length < 1)
            throw new ParsingException("Invalid number of session IDs!");

        size = 12 + 2 * sessionIds.length + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");
    }
}
