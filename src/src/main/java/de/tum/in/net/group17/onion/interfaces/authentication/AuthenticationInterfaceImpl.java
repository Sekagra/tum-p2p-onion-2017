package de.tum.in.net.group17.onion.interfaces.authentication;

import com.google.common.primitives.Shorts;
import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.*;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionTunnelTransportParsedMessage;
import org.apache.log4j.Logger;

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
        this.logger = Logger.getLogger(AuthenticationInterface.class);
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
                int requestID = ((AuthParsedMessage) parsed).getRequestId();
                RequestResult res = results.get(requestID);
                if (res != null) {
                    logger.debug("Received message from authentication module: " + parsed.getClass().getName());
                    res.setReturned(true);
                    res.setResult(parsed);
                    this.results.notifyAll();
                } else {
                    logger.warn("Received message without callback mapping: " + requestID);
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
    public AuthSessionHs1ParsedMessage startSession(Peer peer) throws ParsingException, InterruptedException {
        // Build session start packet
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionStart(requestId, peer.getHostkey());
        this.results.put(requestId, null);

        sendMessage(packet.serialize());

        return waitForSessionResponse(requestId, AuthSessionHs1ParsedMessage.class);
    }

    @Override
    public void closeSession(short sessionId) throws ParsingException {
        ParsedMessage packet = this.parser.buildSessionClose(sessionId);
        this.sendMessage(packet.serialize());
    }

    /**
     * @inheritDoc
     */
    public AuthSessionHs2ParsedMessage forwardIncomingHandshake1(byte[] hs1)  throws ParsingException, InterruptedException  {
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionIncoming1(requestId, hs1);
        this.results.put(requestId, null);

        sendMessage(packet.serialize());

        return waitForSessionResponse(requestId, AuthSessionHs2ParsedMessage.class);
    }

    /**
     * Generic method that waits for a response from all session establish related requests.
     *
     * @param requestId The ID of the request to wait for.
     * @param type The generic parameter to parse the response to.
     *
     * @return The response of the auth module.
     *
     * @throws ParsingException Exception in case anything is wrong with the packet layouts.
     * @throws InterruptedException Exception in case the synchronous waiting is interrupted.
     */
    private <T> T waitForSessionResponse(int requestId, Class<T> type) throws ParsingException, InterruptedException {
        synchronized (this.results) {
            while (!this.results.get(requestId).isReturned())
                this.results.wait(5000);
        }

        if (this.results.get(requestId).isReturned())
            try {
                RequestResult response = this.results.remove(requestId);
                return type.cast(response.getResult());
            } catch (ClassCastException e) {
                throw new ParsingException("Unable to parse response to session start." + e.getMessage());
            }
        else
            throw new ParsingException("Did not receive a response from the auth module to an issued session start.");
    }

    /**
     * @inheritDoc
     */
    public void forwardIncomingHandshake2(short sessionId, byte[] payload) throws ParsingException {
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionIncoming2(requestId, sessionId, payload);
        sendMessage(packet.serialize());
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException {
        return encrypt(message, Arrays.asList(new TunnelSegment[] { segment } ));
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, List<TunnelSegment> segments) throws InterruptedException, ParsingException {
        int requestId = this.requestCounter.getAndAdd(1);
        short[] sessionIds = Shorts.toArray(segments.stream().map(x -> x.getSessionId()).collect(Collectors.toList()));
        ParsedMessage packet = this.parser.buildLayerEncrypt(requestId, sessionIds, message.getData());

        this.results.put(requestId, null);
        sendMessage(packet.serialize());

        return waitForCryptResponse(requestId, message);
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException {
        return decrypt(message, Arrays.asList(new TunnelSegment[] { segment } ));
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, List<TunnelSegment> segments) throws InterruptedException, ParsingException {
        // build the message
        int requestId = this.requestCounter.getAndAdd(1);
        short[] sessionIds = Shorts.toArray(segments.stream().map(x -> x.getSessionId()).collect(Collectors.toList()));
        ParsedMessage packet = this.parser.buildLayerDecrypt(requestId, sessionIds, message.getData());

        this.results.put(requestId, null);
        sendMessage(packet.serialize());

        return waitForCryptResponse(requestId, message);
    }

    /**
     * Generic method that waits for a response from all en- and decryption requests. Sets the payload accordingly.
     *
     * @param requestId The ID of the request to wait for.
     * @param message The message to be en- or decrypted.
     * @return The given message with swapped payload.
     * @throws ParsingException Exception in case anything is wrong with the packet layouts.
     * @throws InterruptedException Exception in case the synchronous waiting is interrupted.
     */
    private OnionTunnelTransportParsedMessage waitForCryptResponse(int requestId, OnionTunnelTransportParsedMessage message) throws ParsingException, InterruptedException {
        // wait for the response
        synchronized (this.results) {
            while (!this.results.get(requestId).isReturned())
                this.results.wait(5000);
        }

        if (this.results.get(requestId).isReturned())
            try {
                AuthLayerCryptResParsedMessage response = (AuthLayerCryptResParsedMessage) this.results.remove(requestId).getResult();
                message.setData(response.getPayload());
                return message;
            } catch (ClassCastException e) {
                throw new ParsingException("Unable to parse response to session layer decrypt." + e.getMessage());
            }
        else
            throw new ParsingException("Did not receive a response from the auth module to an issued session layer decrypt.");
    }
}
