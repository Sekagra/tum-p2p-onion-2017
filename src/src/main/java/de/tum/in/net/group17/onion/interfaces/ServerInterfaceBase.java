package de.tum.in.net.group17.onion.interfaces;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * Base class for all interfaces used for serving functionality to other modules, thus essentially acting as a server only.
 * (this is the case for the interface towards the UI/CM module).
 * Provides a wrapping and basic handling of netty.
 * Created by Christoph Rudolf on 11.06.17.
 */
public abstract class ServerInterfaceBase {
    protected int port;

    /**
     * Start listening to incoming requests on the specified port for this server interface.
     */
    public void listen() {
        EventLoopGroup entryGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(entryGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerChannelInitializer(getHandler()));

            // Bind and start to accept incoming connections.
            b.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            entryGroup.shutdownGracefully();
        }
    }

    /**
     * Retrieve a handler specificly for the interface's implementation.
     * @return A ChannelHandler to be used for this server instance.
     */
    protected abstract ChannelHandler getHandler();
}
