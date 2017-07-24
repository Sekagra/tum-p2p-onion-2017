package de.tum.in.net.group17.onion.interfaces.authentication;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParser;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of an interface to the Onion Authentication module.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class AuthenticationInterfaceImpl extends TcpClientInterface implements AuthenticationInterface {
    private AuthenticationParser parser;
    private ConfigurationProvider config;
    private final AtomicInteger requestCounter;
    private Logger logger;

    /**
     * Create a new authentication interface.
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received from the Onion Authentication module.
     */
    @Inject
    public AuthenticationInterfaceImpl(ConfigurationProvider config, AuthenticationParser parser) {
        super(config.getAuthModuleHost(), config.getAuthModuleRequestPort());
        this.logger = Logger.getLogger(AuthenticationInterface.class);
        this.parser = parser;
        this.config = config;
        this.requestCounter = new AtomicInteger();
        this.requestCounter.set(0);
    }

    public void startSession(Peer peer, final RequestResult callback) {
        // Build session start packet
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = null;
        packet = this.parser.buildSessionStart(requestId, peer.getHostkey());

        // Send the message and parse the retrieved result before passing it back to the callback given
        sendMessage(packet.serialize(), result -> callback.respond(parser.parse(result)));
    }

    public void forwardIncomingHandshake1(Peer peer, ParsedMessage hs1, RequestResult callback) {

    }

    public void forwardIncomingHandshake2(Peer peer, ParsedMessage hs2, RequestResult callback) {

    }

    public void encrypt(Tunnel tunnel, RequestResult callback) {

    }

    public void decrypt(Tunnel tunnel, RequestResult callback) {

    }
}
