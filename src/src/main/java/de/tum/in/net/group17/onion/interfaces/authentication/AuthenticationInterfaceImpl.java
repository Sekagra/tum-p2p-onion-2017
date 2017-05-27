package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.ModuleInterface;
import de.tum.in.net.group17.onion.model.AuthResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParser;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Christoph Rudolf on 27.05.17.
 */
public class AuthenticationInterfaceImpl extends ModuleInterface implements AuthenticationInterface {
    private AuthenticationParser parser;
    private ConfigurationProvider config;
    private final AtomicInteger requestCounter;
    private HashMap<Integer, AuthResult> requests;

    public AuthenticationInterfaceImpl(ConfigurationProvider config, AuthenticationParser parser) {
        this.parser = parser;
        this.config = config;
        this.requests = new HashMap<Integer, AuthResult>();
        this.requestCounter = new AtomicInteger();
        this.requestCounter.set(0);
    }

    public void listen() {
        try {
            super.listen(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new AuthenticationReponseHandler());  // will most likely need access to the requests and parser
                }
            }, this.config.getAuthModuleReponsePort());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startSession(Peer peer, AuthResult callback) {
        // Build session start packet
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionStart(requestId, peer.getHostkey());

        // Establish a connection with the Onion Authentication module to send the data
        Channel channel;
        try {
            channel = super.connect(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {

                }
            }, this.config.getAuthModuleHost(), this.config.getAuthModuleRequestPort());

        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        // Send data
        channel.writeAndFlush(Unpooled.buffer().writeBytes(packet.getData()));

        // todo: shutdown gracefully here?

        // Store callback in association with the request ID to be handled in case of a response
        this.requests.put(requestId, callback);
    }

    public void forwardIncomingHandshake1(Peer peer, ParsedMessage hs1, AuthResult callback) {

    }

    public void forwardIncomingHandshake2(Peer peer, ParsedMessage hs2, AuthResult callback) {

    }

    public void encrypt(Tunnel tunnel, AuthResult callback) {

    }

    public void decrypt(Tunnel tunnel, AuthResult callback) {

    }
}
