package de.tum.in.net.group17.onion.interfaces;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;

/**
 * Base class for all interfaces to other modules. Provides uniform and basic setup of a server using Netty.
 * Created by Christoph Rudolf on 27.05.17.
 */
public abstract class ModuleInterface {

    protected void listen(ChannelHandler handler, int port) throws InterruptedException {
        NioEventLoopGroup acceptorGroup = new NioEventLoopGroup(2);
        NioEventLoopGroup handlerGroup = new NioEventLoopGroup(10);

        ServerBootstrap b = new ServerBootstrap();
        b.group(acceptorGroup, handlerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(handler)
                .option(ChannelOption.SO_BACKLOG, 5)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        //listen to the given response port
        b.localAddress(port).bind().sync();
    }

    protected Channel connect(ChannelHandler handler, InetAddress host, int port) throws InterruptedException {
        return null;
    }
}
