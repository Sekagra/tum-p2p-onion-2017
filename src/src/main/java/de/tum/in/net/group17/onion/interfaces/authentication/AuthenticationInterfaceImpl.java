package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.ModuleInterface;
import de.tum.in.net.group17.onion.model.AuthResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParser;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;


/**
 * Created by Christoph Rudolf on 27.05.17.
 */
public class AuthenticationInterfaceImpl extends ModuleInterface implements AuthenticationInterface {
    private AuthenticationParser parser;
    private ConfigurationProvider config;

    public AuthenticationInterfaceImpl(ConfigurationProvider config, AuthenticationParser parser) {
        this.parser = parser;
        this.config = config;
    }

    protected void listen() {
        try {
            super.listen(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new AuthenticationHandler());
                }
            }, this.config.getAuthModuleReponsePort());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startSession(Peer peer, AuthResult callback) {

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
