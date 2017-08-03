package de.tum.in.net.group17.onion.interfaces.authentication;

import com.google.common.primitives.Shorts;
import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.model.results.RawRequestResult;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.*;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionTunnelTransportParsedMessage;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received from the Onion Authentication module.
     */
    @Inject
    public AuthenticationInterfaceImpl(ConfigurationProvider config, AuthenticationParser parser) {
        super(config.getAuthModuleHost(), config.getAuthModulePort());
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
     * @param data
     */
    private void readResponse(byte[] data) {
        try {
            ParsedMessage parsed = this.parser.parseMsg(data);
            if(parsed instanceof AuthParsedMessage)
            {
                int requestID = ((AuthParsedMessage)parsed).getRequestId();
                RequestResult res = results.get(requestID);
                if(res != null) {
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
        } catch(ParsingException e) {
            logger.warn("Could not parse incoming AUTH message: " + e.getMessage());
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

        synchronized (this.results) {
            while(!this.results.get(requestId).isReturned())
                this.results.wait(5000);
        }

        if(this.results.get(requestId).isReturned())
            try {
                RequestResult response = this.results.remove(requestId);
                return (AuthSessionHs1ParsedMessage)response.getResult();
            } catch (ClassCastException e) {
                throw new ParsingException("Unable to parse response to session start." + e.getMessage());
            }
        else
            throw new ParsingException("Did not receive a response from the auth module to an issued session start.");
    }

    /**
     * @inheritDoc
     */
    public void forwardIncomingHandshake1(Peer peer, ParsedMessage hs1, RequestResult callback) {

    }

    /**
     * @inheritDoc
     */
    public void forwardIncomingHandshake2(Peer peer, short sessionId, byte[] payload) throws ParsingException {
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionIncoming2(requestId, sessionId, payload);
        sendMessage(packet.serialize());
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, Tunnel tunnel) throws InterruptedException, ParsingException {
        int requestId = this.requestCounter.getAndAdd(1);
        short[] sessionIds = Shorts.toArray(tunnel.getSegments().stream().map(x -> x.getSessionId()).collect(Collectors.toList()));
        ParsedMessage packet = this.parser.buildLayerEncrypt(requestId, sessionIds, message.getData());

        this.results.put(requestId, null);
        sendMessage(packet.serialize());

        synchronized (this.results) {
            while(!this.results.get(requestId).isReturned())
                this.results.wait(5000);
        }

        if(this.results.get(requestId).isReturned())
            try {
                AuthLayerEncryptResParsedMessage response = (AuthLayerEncryptResParsedMessage)this.results.remove(requestId).getResult();
                message.setData(response.getPayload());
                return message;
            } catch (ClassCastException e) {
                throw new ParsingException("Unable to parse response to session layer encrypt." + e.getMessage());
            }
        else
            throw new ParsingException("Did not receive a response from the auth module to an issued session layer encrypt.");
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, Tunnel tunnel) throws InterruptedException, ParsingException  {
        // build the message
        int requestId = this.requestCounter.getAndAdd(1);
        short[] sessionIds = Shorts.toArray(tunnel.getSegments().stream().map(x -> x.getSessionId()).collect(Collectors.toList()));
        ParsedMessage packet = this.parser.buildLayerDecrypt(requestId, sessionIds, message.getData());

        this.results.put(requestId, null);
        sendMessage(packet.serialize());

        // wait for the response
        synchronized (this.results) {
            while(!this.results.get(requestId).isReturned())
                this.results.wait(5000);
        }

        if(this.results.get(requestId).isReturned())
            try {
                AuthLayerDecryptResParsedMessage response = (AuthLayerDecryptResParsedMessage)this.results.remove(requestId).getResult();
                message.setData(response.getPayload());
                return message;
            } catch (ClassCastException e) {
                throw new ParsingException("Unable to parse response to session layer decrypt." + e.getMessage());
            }
        else
            throw new ParsingException("Did not receive a response from the auth module to an issued session layer decrypt.");
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException {
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildLayerEncrypt(requestId, new short[] { segment.getSessionId() }, message.getData());

        this.results.put(requestId, null);
        sendMessage(packet.serialize());

        synchronized (this.results) {
            while(!this.results.get(requestId).isReturned())
                this.results.wait(5000);
        }

        if(this.results.get(requestId).isReturned())
            try {
                AuthLayerEncryptResParsedMessage response = (AuthLayerEncryptResParsedMessage)this.results.remove(requestId).getResult();
                message.setData(response.getPayload());
                return message;
            } catch (ClassCastException e) {
                throw new ParsingException("Unable to parse response to session layer encrypt." + e.getMessage());
            }
        else
            throw new ParsingException("Did not receive a response from the auth module to an issued session layer encrypt.");
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException {
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildLayerDecrypt(requestId, new short[] { segment.getSessionId() }, message.getData());

        this.results.put(requestId, null);
        sendMessage(packet.serialize());

        // wait for the response
        synchronized (this.results) {
            while(!this.results.get(requestId).isReturned())
                this.results.wait(5000);
        }

        if(this.results.get(requestId).isReturned())
            try {
                AuthLayerDecryptResParsedMessage response = (AuthLayerDecryptResParsedMessage)this.results.remove(requestId).getResult();
                message.setData(response.getPayload());
                return message;
            } catch (ClassCastException e) {
                throw new ParsingException("Unable to parse response to session layer decrypt." + e.getMessage());
            }
        else
            throw new ParsingException("Did not receive a response from the auth module to an issued session layer decrypt.");
    }
}
