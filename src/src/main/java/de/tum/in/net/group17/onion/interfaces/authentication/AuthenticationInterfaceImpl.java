package de.tum.in.net.group17.onion.interfaces.authentication;

import com.google.common.primitives.Shorts;
import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.*;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionTunnelTransportParsedMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implementation of an interface to the Onion Authentication module.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class AuthenticationInterfaceImpl extends TcpClientInterface implements AuthenticationInterface {
    private AuthenticationParser parser;
    private ConfigurationProvider config;
    private final AtomicInteger requestCounter;
    private Map<Integer, RequestResult> results;
    private Logger logger;

    /**
     * Create a new authentication interface.
     *
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received from the Onion Authentication module.
     */
    @Inject
    public AuthenticationInterfaceImpl(ConfigurationProvider config, AuthenticationParser parser) {
        super(config.getAuthApiHost(), config.getAuthApiPort());
        this.logger = LogManager.getLogger(AuthenticationInterface.class);
        this.parser = parser;
        this.config = config;
        this.requestCounter = new AtomicInteger();
        this.requestCounter.set(0);
        this.results = Collections.synchronizedMap(new HashMap<Integer, RequestResult>());
        setCallback(result -> readResponse(result));
    }

    /**
     * Callback for all messages returned from the authentication module. The data has then be mapped to the requester
     * by the request identifier.
     *
     * @param data A raw data block received back from the authentication module.
     */
    private void readResponse(byte[] data) {
        try {
            ParsedMessage parsed = this.parser.parseMsg(data);
            if (parsed instanceof AuthParsedMessage) {
                int requestId = ((AuthParsedMessage) parsed).getRequestId();
                RequestResult res = results.get(requestId);
                if (res != null) {
                    logger.debug("Received message from authentication module: " + parsed.getClass().getName());
                    res.setResult(parsed);
                    synchronized (res) {
                        res.notify();
                    }
                } else {
                    logger.warn("Received message without callback mapping: " + requestId);
                }
            } else {
                logger.warn("Received SESSION CLOSE message.");
            }
        } catch (ParsingException e) {
            logger.error("Could not parse incoming AUTH message: " + e.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    public AuthSessionHs1ParsedMessage startSession(Peer peer) throws ParsingException, InterruptedException, AuthException {
        this.logger.debug("Starting session via authentication module.");
        // Build session start packet
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionStart(requestId, peer.getHostkey());
        this.results.put(requestId, new RequestResult());

        sendMessage(packet.serialize());

        return waitForSessionResponse(requestId, AuthSessionHs1ParsedMessage.class);
    }

    @Override
    public void closeSession(short sessionId) throws ParsingException {
        this.logger.debug("Closing session via authentication module.");
        ParsedMessage packet = this.parser.buildSessionClose(sessionId);
        this.sendMessage(packet.serialize());
    }

    /**
     * @inheritDoc
     */
    public AuthSessionHs2ParsedMessage forwardIncomingHandshake1(byte[] hs1) throws ParsingException, InterruptedException, AuthException {
        this.logger.debug("Forwarding session handshake 1 to local auth module.");
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionIncoming1(requestId, hs1);
        this.results.put(requestId, new RequestResult());

        sendMessage(packet.serialize());

        return waitForSessionResponse(requestId, AuthSessionHs2ParsedMessage.class);
    }

    /**
     * @inheritDoc
     */
    public void forwardIncomingHandshake2(short sessionId, byte[] payload) throws ParsingException {
        this.logger.debug("Forwarding session handshake 2 to local auth module.");
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionIncoming2(requestId, sessionId, payload);
        sendMessage(packet.serialize());
    }


    /**
     * Generic method that waits for a response from all session establish related requests.
     *
     *
     * @param requestId The ID of the request to wait for.
     * @param type The generic parameter to parse the response to.
     *
     * @return The response of the auth module.
     *
     * @throws ParsingException Exception in case anything is wrong with the packet layouts.
     * @throws InterruptedException Exception in case the synchronous waiting is interrupted.
     * @throws AuthException If an error is returned by the Onion Auth module or we received an unexpected message.
     */
    private <T> T waitForSessionResponse(int requestId, Class<T> type) throws ParsingException, InterruptedException, AuthException {
        if (this.results.get(requestId) == null) {
            throw new AuthException("Request no longer present in waiting list.");
        }
        // wait for the response
        RequestResult res = this.results.get(requestId);
        synchronized (res) {
            res.wait(5000);
        }

        if (res.isReturned()) {
            this.results.remove(requestId);
            AuthParsedMessage msg = (AuthParsedMessage) res.getResult();
            switch(msg.getType()) {
                case AUTH_SESSION_HS1:
                case AUTH_SESSION_HS2:
                    try {
                        return type.cast(res.getResult());
                    } catch (ClassCastException e) {
                        throw new ParsingException("Did not receive the expected message type for this session handshake request." + e.getMessage());
                    }
                case AUTH_ERROR:
                    throw new AuthException("Received AUTH ERROR from request: " + msg.getRequestId());
                default:
                    throw new AuthException("Unexpected message type " + msg.getType().toString() + " at when waiting for handshake response.");
            }
        } else {
            this.results.remove(requestId);
            throw new AuthException("Did not receive a response from the auth module to an issued session start in time.");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment, boolean isCipher) throws InterruptedException, ParsingException, AuthException {
        this.logger.debug("Encrypting data (single hop). Is cipher: " + isCipher);
        int requestId = this.requestCounter.getAndAdd(1);

        ParsedMessage packet = this.parser.buildCipherEncrypt(isCipher, requestId, segment.getSessionId(), message.getData());

        this.results.put(requestId, new RequestResult());
        sendMessage(packet.serialize());

        return waitForCryptResponse(requestId, message);
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, List<TunnelSegment> segments) throws InterruptedException, ParsingException, AuthException {
        this.logger.debug("Encrypting data for a whole tunnel.");
        int requestId = this.requestCounter.getAndAdd(1);

        // Get session IDs in reverse order cause the specification says:
        // "The layered encryption is then to be done by first encrypting the payload with the session
        // key corresponding to session ID 1, followed by with that of session ID 2 and so on."
        // ... and the first/inner encryption has to be done with the session key of the tunnel end
        List<Short> sessionList = segments.stream().map(x -> x.getSessionId()).collect(Collectors.toList());
        Collections.reverse(sessionList);
        short[] sessionIds = Shorts.toArray(sessionList);
        ParsedMessage packet = this.parser.buildLayerEncrypt(requestId, sessionIds, message.getData());

        this.results.put(requestId, new RequestResult());
        sendMessage(packet.serialize());

        return waitForCryptResponse(requestId, message);
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException, AuthException {
        this.logger.debug("Decrypting data for a single hop.");
        int requestId = this.requestCounter.getAndAdd(1);

        ParsedMessage packet = this.parser.buildCipherDecrypt(requestId, segment.getSessionId(), message.getData());

        this.results.put(requestId, new RequestResult());
        sendMessage(packet.serialize());

        return waitForCryptResponse(requestId, message);
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, List<TunnelSegment> segments) throws InterruptedException, ParsingException, AuthException {
        this.logger.debug("Decrypting data for a whole tunnel.");
        // build the message
        int requestId = this.requestCounter.getAndAdd(1);

        // Get session IDs in reverse order cause the specification says:
        // "That is the session key corresponding to session ID N will be used to decrypt
        // one layer from the payload, followed by that of session N-1 and so on."
        // ... and the first/outer decryption has to be done with the session key of our first tunnel segment.
        List<Short> sessionList = segments.stream().map(x -> x.getSessionId()).collect(Collectors.toList());
        Collections.reverse(sessionList);
        short[] sessionIds = Shorts.toArray(sessionList);

        ParsedMessage packet = this.parser.buildLayerDecrypt(requestId, sessionIds, message.getData());

        this.results.put(requestId, new RequestResult());
        sendMessage(packet.serialize());

        return waitForCryptResponse(requestId, message);
    }

    /**
     * Generic method that waits for a response from all en- and decryption requests. Sets the payload accordingly.
     *
     *
     * @param requestId The ID of the request to wait for.
     * @param message The message to be en- or decrypted.
     *
     * @return The given message with swapped payload.
     *
     * @throws ParsingException Exception in case anything is wrong with the packet layouts.
     * @throws InterruptedException Exception in case the synchronous waiting is interrupted.
     * @throws AuthException If an error is returned by the Onion Auth module or we received an unexpected message.
     */
    private OnionTunnelTransportParsedMessage waitForCryptResponse(int requestId, OnionTunnelTransportParsedMessage message) throws ParsingException, InterruptedException, AuthException {
        if (this.results.get(requestId) == null) {
            throw new AuthException("Request no longer present in waiting list.");
        }
        // wait for the response
        RequestResult res = this.results.get(requestId);
        synchronized (res) {
            res.wait(5000);
        }

        if (res.isReturned()) {
            this.results.remove(requestId);
            AuthParsedMessage msg = (AuthParsedMessage) res.getResult();
            switch(msg.getType()) {
                case AUTH_LAYER_ENCRYPT_RESP:
                case AUTH_LAYER_DECRYPT_RESP:
                case AUTH_CIPHER_ENCRYPT_RESP:
                case AUTH_CIPHER_DECRYPT_RESP:
                    message.setData(((AuthCryptResParsedMessage)msg).getPayload());
                    return message;
                case AUTH_ERROR:
                    throw new AuthException("Received AUTH ERROR from request: " + msg.getRequestId());
                default:
                    throw new AuthException("Unexpected message type " + msg.getType().toString() + " at when waiting for crypt response.");
            }
        } else {
            this.results.remove(requestId);
            throw new ParsingException("Did not receive a response from the auth module to an issued session layer decrypt in time.");
        }
    }
}
