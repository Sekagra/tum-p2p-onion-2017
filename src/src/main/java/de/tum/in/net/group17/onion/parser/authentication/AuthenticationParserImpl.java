package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;
import de.tum.in.net.group17.onion.parser.VoidphoneType;

import java.nio.ByteBuffer;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class AuthenticationParserImpl extends VoidphoneParser implements AuthenticationParser {
    public ParsedMessage buildSessionStart(int requestId, byte[] hostkey) {
        int size = 12 + hostkey.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                                   // size
        buffer.putShort(VoidphoneType.AUTH_SESSION_START.getValue());   // AUTH SESSION START
        buffer.putInt(0);                                               // reserved
        buffer.putInt(requestId);                                       // request ID
        buffer.put(hostkey);                                            // hostkey in DER format
        return new ParsedMessage(buffer.array());
    }

    public ParsedMessage buildSessionIncoming1(int requestId, byte[] hostkey, byte[] payload) {
        int size = 14 + hostkey.length + payload.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                                           // size
        buffer.putShort(VoidphoneType.AUTH_SESSION_INCOMING_HS1.getValue());    // AUTH SESSION START
        buffer.putInt(0);                                                       // reserved
        buffer.putInt(requestId);                                               // request ID
        buffer.putShort((short)hostkey.length);                                 // hostkey size
        buffer.put(hostkey);                                                    // hostkey in DER format
        buffer.put(payload);                                                    // incoming payload from other onion auth
        return new ParsedMessage(buffer.array());
    }

    public ParsedMessage buildSessionIncoming2(int requestId, short sessionId, byte[] payload) {
        int size = 12 + payload.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                                           // size
        buffer.putShort(VoidphoneType.AUTH_SESSION_INCOMING_HS2.getValue());    // AUTH SESSION START
        buffer.putShort((short)0);                                              // reserved
        buffer.putShort(sessionId);                                             // reserved
        buffer.put(payload);                                                    // incoming payload from other onion auth
        return new ParsedMessage(buffer.array());
    }

    public ParsedMessage buildLayerEncrypt(int requestId, short[] sessionIds, byte[] payload) {
        return buildLayerCryptMessage(VoidphoneType.AUTH_LAYER_ENCRYPT, requestId, sessionIds, payload);
    }

    public ParsedMessage buildLayerDecrypt(int requestId, short[] sessionIds, byte[] payload) {
        return buildLayerCryptMessage(VoidphoneType.AUTH_LAYER_DECRYPT, requestId, sessionIds, payload);
    }

    private ParsedMessage buildLayerCryptMessage(VoidphoneType type, int requestId, short[] sessionIds, byte[] payload) {
        // calculate whole size
        // header + reserved + layer number + request ID + session IDs + payload
        int size = 12 + 2 * sessionIds.length + payload.length;
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
        return new ParsedMessage(buffer.array());
    }

    public ParsedMessage buildSessionClose(short sessionId) {
        int size = 8;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short)size);                                   // size
        buffer.putShort(VoidphoneType.AUTH_SESSION_CLOSE.getValue());   // AUTH SESSION CLOSE
        buffer.putShort((short)0);                                      // reserved
        buffer.putShort(sessionId);                                     // session ID
        return new ParsedMessage(buffer.array());
    }

    public ParsedMessage parseSessionHandshake1(byte[] message) {
        if(checkSize(message) && checkType(message, VoidphoneType.AUTH_SESSION_HS1)) {
            return new ParsedMessage(message);
        }
        return null;
    }

    public ParsedMessage parseSessionHandshake2(byte[] message) {
        if(checkSize(message) && checkType(message, VoidphoneType.AUTH_SESSION_HS2)) {
            return new ParsedMessage(message);
        }
        return null;
    }

    public ParsedMessage parseLayerEncryptResponse(byte[] message) {
        if(checkSize(message) && checkType(message, VoidphoneType.AUTH_LAYER_ENCRYPT_RESP)) {
            return new ParsedMessage(message);
        }
        return null;
    }

    public ParsedMessage parseLayerDecryptResponse(byte[] message) {
        if(checkSize(message) && checkType(message, VoidphoneType.AUTH_LAYER_DECRYPT_RESP)) {
            return new ParsedMessage(message);
        }
        return null;
    }
}
