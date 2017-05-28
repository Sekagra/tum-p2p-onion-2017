package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsingException;

import java.nio.ByteBuffer;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class AuthenticationParserImpl extends VoidphoneParser implements AuthenticationParser {
    public ParsedMessage buildSessionStart(int requestId, byte[] hostkey) {
        int size = 12 + hostkey.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                                   // size
        buffer.putShort(MessageType.AUTH_SESSION_START.getValue());     // AUTH SESSION START
        buffer.putInt(0);                                               // reserved
        buffer.putInt(requestId);                                       // request ID
        buffer.put(hostkey);                                            // hostkey in DER format
        return createParsedMessage(buffer.array());
    }

    public ParsedMessage buildSessionIncoming1(int requestId, byte[] hostkey, byte[] payload) {
        int size = 14 + hostkey.length + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                                           // size
        buffer.putShort(MessageType.AUTH_SESSION_INCOMING_HS1.getValue());      // AUTH SESSION START
        buffer.putInt(0);                                                       // reserved
        buffer.putInt(requestId);                                               // request ID
        buffer.putShort((short)hostkey.length);                                 // hostkey size
        buffer.put(hostkey);                                                    // hostkey in DER format
        buffer.put(payload);                                                    // incoming payload from other onion auth
        return createParsedMessage(buffer.array());
    }

    public ParsedMessage buildSessionIncoming2(int requestId, short sessionId, byte[] payload) {
        int size = 12 + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                                           // size
        buffer.putShort(MessageType.AUTH_SESSION_INCOMING_HS2.getValue());      // AUTH SESSION START
        buffer.putShort((short)0);                                              // reserved
        buffer.putShort(sessionId);                                             // reserved
        buffer.put(payload);                                                    // incoming payload from other onion auth
        return createParsedMessage(buffer.array());
    }

    public ParsedMessage buildLayerEncrypt(int requestId, short[] sessionIds, byte[] payload) {
        return buildLayerCryptMessage(MessageType.AUTH_LAYER_ENCRYPT, requestId, sessionIds, payload);
    }

    public ParsedMessage buildLayerDecrypt(int requestId, short[] sessionIds, byte[] payload) {
        return buildLayerCryptMessage(MessageType.AUTH_LAYER_DECRYPT, requestId, sessionIds, payload);
    }

    private ParsedMessage buildLayerCryptMessage(MessageType type, int requestId, short[] sessionIds, byte[] payload) {
        // calculate whole size
        // header + reserved + layer number + request ID + session IDs + payload
        int size = 12 + 2 * sessionIds.length + payload.length;
        if(size > 65535)
            throw new ParsingException("Message too large!");

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                   // size
        buffer.putShort(type.getValue());               // AUTH SESSION ENCRYPT/DECRYPT
        buffer.putShort((short)0);                      // reserved
        buffer.putChar((char)sessionIds.length);        // session ID
        buffer.putChar((char)0);                        // reserved
        buffer.putInt(requestId);                       // request ID
        for (int i=0; i < sessionIds.length; i++) {     // session ID 1 ...
            buffer.putShort(sessionIds[i]);             // ... session ID n
        }
        buffer.put(payload);                            // encrypted payload
        return createParsedMessage(buffer.array());
    }

    public ParsedMessage buildSessionClose(short sessionId) {
        int size = 8; // Size is static => We do not have to check it
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                                   // size
        buffer.putShort(MessageType.AUTH_SESSION_CLOSE.getValue());     // AUTH SESSION CLOSE
        buffer.putShort((short)0);                                      // reserved
        buffer.putShort(sessionId);                                     // session ID
        return createParsedMessage(buffer.array());
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_SESSION_HS1 message.
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity, null if the format is violated.
     */
    private ParsedMessage parseSessionHandshake1(byte[] message) {
        MessageType type = MessageType.AUTH_SESSION_HS1;
        checkSize(message); // Throws an exception if an error occurs
        checkType(message, type); // Will throw a parsing exception on any error
        return createParsedMessage(message);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_SESSION_HS2 message.
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity, null if the format is violated.
     */
    private ParsedMessage parseSessionHandshake2(byte[] message) {
        MessageType type = MessageType.AUTH_SESSION_HS2;
        checkSize(message); // Throws an exception if an error occurs
        checkType(message, type); // Will throw a parsing exception on any error
        return createParsedMessage(message);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_LAYER_ENCRYPT_RESP message.
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity, null if the format is violated.
     */
    private ParsedMessage parseLayerEncryptResponse(byte[] message) {
        MessageType type = MessageType.AUTH_LAYER_ENCRYPT_RESP;
        checkSize(message); // Throws an exception if an error occurs
        checkType(message, type); // Will throw a parsing exception on any error
        return createParsedMessage(message);
    }

    /**
     * Confirm the size and type matching the specification of the AUTH_LAYER_DECRYPT_RESP message.
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity, null if the format is violated.
     */
    private ParsedMessage parseLayerDecryptResponse(byte[] message) {
        MessageType type = MessageType.AUTH_LAYER_DECRYPT_RESP;
        checkSize(message); // Throws an exception if an error occurs
        checkType(message, type); // Will throw a parsing exception on any error
        return createParsedMessage(message);
    }

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
