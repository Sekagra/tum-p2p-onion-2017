package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.Parser;
import de.tum.in.net.group17.onion.parser.ParsingException;

/**
 * Created by Christoph Rudolf on 24.05.17.
 *
 * Marko Dorfhuber (PraMiD) 24.07.2017: Added Cipher Messages
 */
public interface AuthenticationParser extends Parser {
    /**
     * Build the message used to issue the start of a a new session to the Onion Auth module.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param hostkey The host key of the hop which is chosen as communication partner for this session.
     * @return The AUTH_SESSION_START message conform to the specification.
     */
    ParsedMessage buildSessionStart(int requestId, byte[] hostkey) throws ParsingException;

    /**
     * Build the forwarding message to hand over an incoming first handshake message from another
     * Onion module to the own Onion Auth.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param payload The payload of the first Onion Auth handshake message.
     * @return The AUTH_SESSION_INCOMING_HS1 message conform to the specification.
     */
    ParsedMessage buildSessionIncoming1(int requestId, byte[] payload) throws ParsingException;

    /**
     * Build the forwarding message to hand over an incoming second handshake message from another
     * Onion module to the own Onion Auth.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param sessionId ID of the newly established session.
     * @param payload The payload of the first Onion Auth handshake message.
     * @return The AUTH_SESSION_INCOMING_HS2 message conform to the specification.
     */
    ParsedMessage buildSessionIncoming2(int requestId, short sessionId, byte[] payload) throws ParsingException;

    /**
     * Build the message to issue the encryption of data for a certain tunnel which uses a list of sessions.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param sessionIds The IDs of sessions involved in the path to the target in correct order.
     * @param payload The payload to encrypt.
     * @return The AUTH_LAYER_ENCRYPT message conform to the specification.
     */
    ParsedMessage buildLayerEncrypt(int requestId, short[] sessionIds, byte[] payload) throws ParsingException;

    /**
     * Build the message to issue the decryption of data for a certain tunnel which uses a list of sessions.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param sessionIds The IDs of sessions involved in the path to the target in correct order.
     * @param payload The payload to decrypt.
     * @return The AUTH_LAYER_DECRYPT message conform to the specification.
     */
    ParsedMessage buildLayerDecrypt(int requestId, short[] sessionIds, byte[] payload) throws ParsingException;

    /**
     * Create a new AUTH_CIPHER_ENCRYPT message with the given parameters.
     *
     * @param stillEncrypted Flag that indicates if the message is encrypted the first time.
     * @param requestId The request ID that shall be used.
     * @param sessionId The session ID used for decryption.
     * @param payload The payload that shall be encrypted.
     *
     * @return An AuthCipherEncryptParsedMessage that is conform to the specification.
     */
    ParsedMessage buildCipherEncrypt(boolean stillEncrypted, int requestId, short sessionId, byte[] payload) throws ParsingException;

    /**
     * Create a new AUTH_CIPHER_DECRYPT message with the given parameters.
     *
     * @param stillEncrypted Flag that indicates if the message is still encrypted after removing
     *                       the last layer of encryption.
     * @param requestId The request ID that shall be used.
     * @param sessionId The session ID used for decryption.
     * @param payload The payload that shall be decrypted.
     *
     * @return An AuthCipherDecryptParsedMessage that is conform to the specification.
     */
    ParsedMessage buildCipherDecrypt(boolean stillEncrypted, int requestId, short sessionId, byte[] payload) throws ParsingException;

    /**
     * Build the message to close a session held by the authentication module.
     * @param sessionId The ID of the session to terminate.
     * @return The AUTH_SESSION_CLOSE message conform to the specification.
     */
    ParsedMessage buildSessionClose(short sessionId) throws ParsingException;
}
