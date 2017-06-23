package de.tum.in.net.group17.onion.interfaces.onion;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.UdpServerInterfaceBase;
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
    private UdpServerInterfaceBase server;

    public OnionInterfaceImpl(ConfigurationProvider config, OnionToOnionParser parser) {
        this.parser = parser;
        this.config = config;
        this.port = this.config.getOnionPort();
        this.server = new UdpServerInterfaceBase();
    }

    /**
     * Start listening for incoming Onion UDP packets.
     */
    public void listen()
    {
        this.server.listen(this.port, new SimpleChannelInboundHandler<DatagramPacket>() {
            @Override
            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                super.channelRegistered(ctx);
            }

            @Override
            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                super.channelUnregistered(ctx);
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                super.channelInactive(ctx);
            }

            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                super.channelReadComplete(ctx);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                super.channelRead(ctx, msg);
            }

            @Override
            public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
                System.out.println ("Messaged received on " + new Date() + ":\r");
                final ByteBuf bb = packet.content();
                byte[] buf = new byte[bb.readableBytes()];

                bb.readBytes(buf);

                System.out.println("Received [" + new String(buf)+ "]");
            }
        });
    }
}
