package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.RequestInterfaceBase;
import de.tum.in.net.group17.onion.model.AuthResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of an interface to the Onion Authentication module.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class AuthenticationInterfaceImpl extends RequestInterfaceBase implements AuthenticationInterface {
    private AuthenticationParser parser;
    private ConfigurationProvider config;
    private final AtomicInteger requestCounter;
    //private HashMap<Integer, AuthResult> requests;

    public AuthenticationInterfaceImpl(ConfigurationProvider config, AuthenticationParser parser) {
        this.parser = parser;
        this.config = config;
        this.requestCounter = new AtomicInteger();
        this.requestCounter.set(0);
    }

    public void startSession(Peer peer, final AuthResult callback) {
        // Build session start packet
        int requestId = this.requestCounter.getAndAdd(1);
        ParsedMessage packet = this.parser.buildSessionStart(requestId, peer.getHostkey());

        // Get channel with the correct response handler
        Channel channel = null;
        try {
            channel = this.getChannel(new SimpleChannelInboundHandler() {
                @Override
                protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                    ByteBuf buf = (ByteBuf) o;
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    callback.respond(parser.parse(bytes));  //move parsing to pipeline
                }
            }, this.config.getAuthModuleHost(), this.config.getAuthModuleRequestPort());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Send data
        channel.writeAndFlush(Unpooled.buffer().writeBytes(packet.serialize()));
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
