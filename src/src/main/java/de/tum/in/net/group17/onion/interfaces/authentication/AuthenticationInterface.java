package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.model.AuthResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * This interface is responsible for maintaining the connection to the Onion authentication module.
 * It will keep track of issued requests and maps asynchronous answers back.
 * Created by Christoph Rudolf on 27.05.17.
 */
public interface AuthenticationInterface {

    /**
     * Triggers the interface to build a connection to the module it is targeted to interface with.
     */
    void listen();

    /**
     * Issue the start of a new session on the Onion Authentication module.
     * @param peer The peer to start a new session with.
     * @param callback A callback function to be called once an answers has been retrieved.
     */
    void startSession(Peer peer, AuthResult callback);

    /**
     * Forward a received handshake initiation packet to the Onion module.
     * @param peer The peer with whom a session is initiated.
     * @param callback A callback function to be called once an answers has been retrieved.
     */
    void forwardIncomingHandshake1(Peer peer, ParsedMessage hs1, AuthResult callback);

    /**
     * Forward a received handshake initiation packet to the Onion module.
     * @param peer The peer with whom a session is initiated.
     * @param callback A callback function to be called once an answers has been retrieved.
     */
    void forwardIncomingHandshake2(Peer peer, ParsedMessage hs2, AuthResult callback);

    /**
     * Order the authentication module to encrypt data for a given tunnel.
     * @param tunnel The tunnel for which this message has to be encrypted. This defines the target and all hops.
     * @param callback A callback for delivery of the encrypted data.
     */
    void encrypt(Tunnel tunnel, AuthResult callback);

    /**
     * Order the authentication module to decrypt data for a given tunnel.
     * @param tunnel The tunnel for which this message has to be decrypted. This defines the target and all hops.
     * @param callback A callback for delivery of the decrypted data.
     */
    void decrypt(Tunnel tunnel, AuthResult callback);
}
