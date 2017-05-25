package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * Created by Christoph Rudolf on 24.05.17.
 */
public interface AuthenticationParser {
    /**
     * Build the message used to issue the start of a a new session to the Onion Auth module.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param hostkey The host key of the hop which is chosen as communication partner for this session.
     * @return The AUTH_SESSION_START message conform to the specification.
     */
    ParsedMessage buildSessionStart(int requestId, byte[] hostkey);

    /**
     * Build the forwarding message to hand over an incoming first handshake message from another
     * Onion module to the own Onion Auth.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param hostkey Key of the source host of this incoming handshake message.
     * @param payload The payload of the first Onion Auth handshake message.
     * @return The AUTH_SESSION_INCOMING_HS1 message conform to the specification.
     */
    ParsedMessage buildSessionIncoming1(int requestId, byte[] hostkey, byte[] payload);

    /**
     * Build the forwarding message to hand over an incoming second handshake message from another
     * Onion module to the own Onion Auth.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param sessionId ID of the newly established session.
     * @param payload The payload of the first Onion Auth handshake message.
     * @return The AUTH_SESSION_INCOMING_HS2 message conform to the specification.
     */
    ParsedMessage buildSessionIncoming2(int requestId, short sessionId, byte[] payload);

    /**
     * Build the message to issue the encryption of data for a certain tunnel which uses a list of sessions.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param sessionIds The IDs of sessions involved in the path to the target in correct order.
     * @param payload The payload to encrypt.
     * @return The AUTH_LAYER_ENCRYPT message conform to the specification.
     */
    ParsedMessage buildLayerEncrypt(int requestId, short[] sessionIds, byte[] payload);

    /**
     * Build the message to issue the decryption of data for a certain tunnel which uses a list of sessions.
     * @param requestId A unique identifier used to track possible answers to this request.
     * @param sessionIds The IDs of sessions involved in the path to the target in correct order.
     * @param payload The payload to decrypt.
     * @return The AUTH_LAYER_DECRYPT message conform to the specification.
     */
    ParsedMessage buildLayerDecrypt(int requestId, short[] sessionIds, byte[] payload);

    /**
     * Build the message to close a session held by the authentication module.
     * @param sessionId The ID of the session to terminate.
     * @return The AUTH_SESSION_CLOSE message conform to the specification.
     */
    ParsedMessage buildSessionClose(short sessionId);

    /**
     * Confirm the size and type matching the specification of the AUTH_SESSION_HS1 message.
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity, null if the format is violated.
     */
    ParsedMessage parseSessionHandshake1(byte[] message);

    /**
     * Confirm the size and type matching the specification of the AUTH_SESSION_HS2 message.
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity, null if the format is violated.
     */
    ParsedMessage parseSessionHandshake2(byte[] message);

    /**
     * Confirm the size and type matching the specification of the AUTH_LAYER_ENCRYPT_RESP message.
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity, null if the format is violated.
     */
    ParsedMessage parseLayerEncryptResponse(byte[] message);

    /**
     * Confirm the size and type matching the specification of the AUTH_LAYER_DECRYPT_RESP message.
     * @param message The raw message as byte[].
     * @return The parsed message to confirm its validity, null if the format is violated.
     */
    ParsedMessage parseLayerDecryptResponse(byte[] message);

}
