package de.tum.in.net.group17.onion.interfaces.authentication;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.model.results.RawRequestResult;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.AuthParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParser;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of an interface to the Onion Authentication module.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class AuthenticationInterfaceImpl extends TcpClientInterface implements AuthenticationInterface {
    private AuthenticationParser parser;
    private ConfigurationProvider config;
    private final AtomicInteger requestCounter;
    private Map<Integer, RequestResult> callbacks;
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
        this.callbacks = Collections.synchronizedMap(new HashMap<Integer, RequestResult>());
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
                RequestResult cb = callbacks.remove(requestID);
                if(cb != null) {
                    logger.debug("Received message from authentication module: " + parsed.getClass().getName());
                    cb.respond(parsed);
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
    public void startSession(Peer peer, final RequestResult callback) throws ParsingException {
        // Build session start packet
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = null;

        packet = this.parser.buildSessionStart(requestId, peer.getHostkey());
        this.callbacks.put(requestId, callback);

        sendMessage(packet.serialize());
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
    public void encrypt(Tunnel tunnel, RequestResult callback) {

    }

    /**
     * @inheritDoc
     */
    public void decrypt(Tunnel tunnel, RequestResult callback) {

    }
}
