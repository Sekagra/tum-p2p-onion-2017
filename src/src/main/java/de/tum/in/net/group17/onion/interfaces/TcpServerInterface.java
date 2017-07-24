package de.tum.in.net.group17.onion.interfaces;

import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

/**
 * Base class for all interfaces used for serving functionality to other modules, thus essentially acting as a server only.
 * (this is the case for the interface towards the UI/CM module).
 * Provides a wrapping and basic handling of netty.
 * Created by Christoph Rudolf on 11.06.17.
 */
public abstract class TcpServerInterface {
    private Logger logger;

    public TcpServerInterface() {
        this.logger = Logger.getLogger(OnionApiInterface.class);
    }

    /**
     * Start listening to incoming requests on the specified port for this server interface.
     */
    public void listen(int port) {
        EventLoopGroup entryGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(entryGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerChannelInitializer(() -> getHandler()));

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
     * Read a plain unparsed message from the TCP channel.
     * @param msg The data to be read by the receiver.
     */
    protected abstract void readIncoming(byte[] msg);

    /**
     * Retrieve a handler specifically for the interface's implementation.
     * This is encapsulated by a separate method as Netty needs a new handler instance for every client.
     * @return A ChannelHandler to be used for a new connection.
     */
    protected ChannelHandler getHandler() {
        return new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                logger.info(ctx.channel().remoteAddress().toString() + " has connected (Active).");
            }

            @Override
            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                super.channelRegistered(ctx);
                logger.info(ctx.channel().remoteAddress().toString() + " has connected (Register).");
            }

            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                readIncoming(byteBuf.array());
            }

            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
                ctx.fireChannelReadComplete();
            }

            @Override
            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                super.channelUnregistered(ctx);
                logger.info(ctx.channel().remoteAddress().toString() + " has disconnected (Unregister).");
            }
        };
    }
}
