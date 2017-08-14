package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs1ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs2ParsedMessage;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionTunnelTransportParsedMessage;

import java.util.List;

/**
 * This interface is responsible for maintaining the connection to the Onion authentication module.
 * It encapsulates all interactions with the authentication module.
 * Created by Christoph Rudolf on 27.05.17.
 */
public interface AuthenticationInterface {
    /**
     * Issue the start of a new session on the Onion Authentication module (synchronized).
     * @param peer The peer to start a new session with.
     * @return
     */
    AuthSessionHs1ParsedMessage startSession(Peer peer) throws ParsingException, InterruptedException, AuthException;

    /**
     * Notify the auth module that a given session can be closed now.
     * @param sessionId The ID of the session to close.
     */
    void closeSession(short sessionId) throws ParsingException;

    /**
     * Forward a received handshake initiation packet to the Onion module.
     * @param payload The handshake payload given by the peers authentication module in AUTH SESSION HS1.
     */
    AuthSessionHs2ParsedMessage forwardIncomingHandshake1(byte[] payload) throws ParsingException, InterruptedException, AuthException;

    /**
     * Forward a received handshake initiation packet to the Onion module.
     * @param sessionId ID of the previously started session this handshake message belongs to.
     * @param payload The payload obtained by an incoming AUTH SESSION HS2 message.
     */
    void forwardIncomingHandshake2(short sessionId, byte[] payload) throws ParsingException;

    /**
     * Order the authentication module to decrypt data for a single layer (used by intermediate hops).
     * @param message Plain OnionTunnelTransportParsedMessage to be decrypted with all sessions in the given tunnel.
     * @param segment A segment for which this message has to be layer-encrypted once.
     * @param isCipher Flag to specify whether or not we are the cipher or plain data not.
     *
     * @return The incoming message but with decrypted data.
     *
     * @throws ParsingException Exception in case anything is wrong with the packet layouts.
     * @throws InterruptedException Exception in case the synchronous waiting is interrupted.
     */
    OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment, boolean isCipher) throws InterruptedException, ParsingException, AuthException;

    /**
     * Order the authentication module to encrypt data for a single layer.
     * @param message Plain OnionTunnelTransportParsedMessage to be encrypted with all sessions in the given tunnel.
     * @param segments A list of segments for which this message has to be layer-encrypted.
     *
     * @return The incoming message but with encrypted data.
     *
     * @throws ParsingException Exception in case anything is wrong with the packet layouts.
     * @throws InterruptedException Exception in case the synchronous waiting is interrupted.
     */
    OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, List<TunnelSegment> segments) throws InterruptedException, ParsingException, AuthException;

    /**
     * Order the authentication module to decrypt data for a single layer (used by intermediate hops).
     * @param message Plain OnionTunnelTransportParsedMessage to be decrypted with all sessions in the given tunnel.
     * @param segment A segment for which this message has to be layer-decrypted once.
     *
     * @return The incoming message but with decrypted data.
     *
     * @throws ParsingException Exception in case anything is wrong with the packet layouts.
     * @throws InterruptedException Exception in case the synchronous waiting is interrupted.
     */
    OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException, AuthException;

    /**
     * Order the authentication module to decrypt data for a single layer.
     * @param message Plain OnionTunnelTransportParsedMessage to be decrypted with all sessions in the given tunnel.
     * @param segments A list of segments for which this message has to be layer-decrypted.
     *
     * @return The incoming message but with decrypted data.
     *
     * @throws ParsingException Exception in case anything is wrong with the packet layouts.
     * @throws InterruptedException Exception in case the synchronous waiting is interrupted.
     */
    OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, List<TunnelSegment> segments) throws InterruptedException, ParsingException, AuthException;
}
