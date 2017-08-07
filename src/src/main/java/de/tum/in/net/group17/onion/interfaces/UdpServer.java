package de.tum.in.net.group17.onion.interfaces;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetAddress;
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
        } /*finally {
            try {
                this.channel.closeFuture().sync();
            } catch(InterruptedException ex ) {}

            eventLoopGroup.shutdownGracefully();
        }*/
    }
}
