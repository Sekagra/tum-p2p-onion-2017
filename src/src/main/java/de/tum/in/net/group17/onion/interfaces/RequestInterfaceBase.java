package de.tum.in.net.group17.onion.interfaces;

import de.tum.in.net.group17.onion.model.results.RawRequestResult;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;

/**
 * Base class for all interfaces used for requesting functionality from other modules, thus essentially acting as a client only.
 * (this is the case for RPS and Onion Auth).
 * Provides a wrapping and basic handling of netty.
 * Created by Christoph Rudolf on 05.06.17.
 */
public abstract class RequestInterfaceBase {
    private Channel channel;
    protected InetAddress host;
    protected int port;

    protected Channel getChannel(SimpleChannelInboundHandler handler, InetAddress host, int port) throws InterruptedException {
        if(this.channel != null && this.channel.isOpen() && this.channel.isWritable()) {
            return this.channel;
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ClientChannelInitializer(handler));

            // Start client, wait for the connection and return the channel
            this.channel = b.connect(host, port).sync().channel();
            return this.channel;
        } finally {
            workerGroup.shutdownGracefully();
        }
    }


    protected  void sendMessage(byte[] data, final RawRequestResult callback) {
        // Get channel with the correct response handler
        Channel channel = null;
        try {
            channel = this.getChannel(new SimpleChannelInboundHandler() {
                @Override
                protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                    ByteBuf buf = (ByteBuf) o;
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    callback.respond(bytes);  //move parsing to pipeline
                }
            }, this.host, this.port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Send data
        channel.writeAndFlush(Unpooled.buffer().writeBytes(data));
    }
}
