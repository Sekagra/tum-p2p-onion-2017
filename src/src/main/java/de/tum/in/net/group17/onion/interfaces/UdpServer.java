package de.tum.in.net.group17.onion.interfaces;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Base class for all interfaces needing a server running via UDP. This is currently only the case for the
 * Onion to Onion communication.
 * Provides a wrapping and basic handling of netty.
 * Created by Christoph Rudolf on 21.06.17.
 */
public class UdpServer {
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    /**
     * Start listening to incoming requests on the specified port for this server interface.
     *
     * @param port The port to be listening on.
     * @param handler The message handler for the unparsed.
     */
    public void listen(InetAddress addr, final int port, final UdpMessageHandler handler) {
        final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        System.out.println("UDP Server starting to listen.");

        // Setup ChannelInboundHandler
        SimpleChannelInboundHandler<DatagramPacket> inboundHandler = new SimpleChannelInboundHandler<DatagramPacket>() {
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
                handler.readDatagram(ctx, packet);
            }
        };

        try {
            Bootstrap b = new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("handler", inboundHandler);
                        }
                    });
            this.channel = b.bind(addr, port).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send out a single UDP datagram message to the specified receiver.
     *
     * @param targetIp The IP to send the message to.
     * @param targetPort The targeted port on the receivers end.
     * @param data The raw data to send to.
     * @throws IOException Throws IOException if it isn't possible to send the message due to socket issues.
     */
    public void send(InetAddress targetIp, int targetPort, byte[] data) throws IOException {
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), new InetSocketAddress(targetIp, targetPort)));
    }
}
