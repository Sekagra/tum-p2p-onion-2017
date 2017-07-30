package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;

/**
 * This interface is responsible for maintaining the connection to the Onion authentication module.
 * It encapsulates all interactions with the authentication module.
 * Created by Christoph Rudolf on 27.05.17.
 */
public interface AuthenticationInterface {
    /**
     * Issue the start of a new session on the Onion Authentication module.
     * @param peer The peer to start a new session with.
     * @param callback A callback function to be called once an answer has been retrieved.
     */
    void startSession(Peer peer, RequestResult callback) throws ParsingException;

    /**
     * Forward a received handshake initiation packet to the Onion module.
     * @param peer The peer with whom a session is initiated.
     * @param callback A callback function to be called once an answer has been retrieved.
     */
    void forwardIncomingHandshake1(Peer peer, ParsedMessage hs1, RequestResult callback);

    /**
     * Forward a received handshake initiation packet to the Onion module.
     * @param peer The peer with whom a session is initiated.
     * @param sessionId ID of the previously started session this handshake message belongs to.
     * @param payload The payload obtained by an incoming AUTH SESSION HS2 message.
     */
    void forwardIncomingHandshake2(Peer peer, short sessionId, byte[] payload) throws ParsingException;

    /**
     * Order the authentication module to encrypt data for a given tunnel.
     * @param tunnel The tunnel for which this message has to be encrypted. This defines the target and all hops.
     * @param callback A callback for delivery of the encrypted data.
     */
    void encrypt(Tunnel tunnel, RequestResult callback);

    /**
     * Order the authentication module to decrypt data for a given tunnel.
     * @param tunnel The tunnel for which this message has to be decrypted. This defines the target and all hops.
     * @param callback A callback for delivery of the decrypted data.
     */
    void decrypt(Tunnel tunnel, RequestResult callback);
}
