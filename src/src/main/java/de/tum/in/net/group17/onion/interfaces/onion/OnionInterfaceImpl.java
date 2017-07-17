package de.tum.in.net.group17.onion.interfaces.onion;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.config.ConfigurationProviderImpl;
import de.tum.in.net.group17.onion.interfaces.UdpClient;
import de.tum.in.net.group17.onion.interfaces.UdpMessageHandler;
import de.tum.in.net.group17.onion.interfaces.UdpServer;
import de.tum.in.net.group17.onion.interfaces.authentication.AuthenticationInterface;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionToOnionParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.Date;

/**
 * Implementation of the Onion to Onion interface via UDP.
 * Created by Christoph Rudolf on 21.06.17.
 */
public class OnionInterfaceImpl implements OnionInterface {
    private ConfigurationProvider config;
    private OnionToOnionParser parser;
    private int port;
    private UdpServer server;
    private UdpClient client;
    private AuthenticationInterface authInterface;

    @Inject
    public OnionInterfaceImpl(ConfigurationProvider config, OnionToOnionParser parser, AuthenticationInterface authInterface) {
        this.parser = parser;
        this.config = config;
        this.port = this.config.getOnionPort();
        this.server = new UdpServer();
        this.client = new UdpClient();
        this.authInterface = authInterface;
    }

    /**
     * Start listening for incoming Onion UDP packets.
     */
    @Override
    public void listen()
    {
        this.server.listen(this.port, (ctx, packet) -> {
            System.out.println ("WAT");

            System.out.println ("Messaged received on " + new Date() + " from " + packet.sender().toString() + "\r");
            final ByteBuf bb = packet.content();
            byte[] buf = new byte[bb.readableBytes()];

            bb.readBytes(buf);

            System.out.println("Received [" + new String(buf)+ "]");
        });
    }
}
