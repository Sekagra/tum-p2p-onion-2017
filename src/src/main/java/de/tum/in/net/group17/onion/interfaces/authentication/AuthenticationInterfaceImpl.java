package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.RequestInterfaceBase;
import de.tum.in.net.group17.onion.model.results.RawRequestResult;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParser;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of an interface to the Onion Authentication module.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class AuthenticationInterfaceImpl extends RequestInterfaceBase implements AuthenticationInterface {
    private AuthenticationParser parser;
    private ConfigurationProvider config;
    private final AtomicInteger requestCounter;

    public AuthenticationInterfaceImpl(ConfigurationProvider config, AuthenticationParser parser) {
        this.parser = parser;
        this.config = config;
        this.requestCounter = new AtomicInteger();
        this.requestCounter.set(0);
        this.host = this.config.getAuthModuleHost();
        this.port = this.config.getAuthModuleRequestPort();
    }

    public void startSession(Peer peer, final RequestResult callback) {
        // Build session start packet
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionStart(requestId, peer.getHostkey());

        // Send the message and parse the retrieved result before passing it back to the callback given
        sendMessage(packet.serialize(), new RawRequestResult() {
            public void respond(byte[] result) {
                callback.respond(parser.parse(result));
            }
        });
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