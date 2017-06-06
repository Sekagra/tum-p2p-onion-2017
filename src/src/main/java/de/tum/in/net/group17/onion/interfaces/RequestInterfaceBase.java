package de.tum.in.net.group17.onion.interfaces;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;

/**
 * Base class for all interfaces requesting functionality from other modules.
 * Provides a wrapping and basic handling of netty.
 * Created by Christoph Rudolf on 05.06.17.
 */
public class RequestInterfaceBase {
    private Channel channel;

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
}
