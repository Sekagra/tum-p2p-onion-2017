package de.tum.in.net.group17.onion.interfaces;

import de.tum.in.net.group17.onion.interfaces.authentication.AuthenticationInterface;
import de.tum.in.net.group17.onion.model.results.RawRequestResult;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;

import java.net.ConnectException;
import java.net.InetAddress;

/**
 * Base class for all interfaces used for requesting functionality from other modules, thus essentially acting as a client only.
 * (this is the case for RPS and Onion Auth).
 * Provides a wrapping and basic handling of netty.
 * Created by Christoph Rudolf on 05.06.17.
 */
public class TcpClientInterface {
    private Channel channel;
    protected InetAddress host;
    protected int port;
    private RawRequestResult callback;
    private Logger logger;

    public TcpClientInterface(InetAddress host, int port) {
        this.logger = Logger.getLogger(AuthenticationInterface.class);
        this.host = host;
        this.port = port;
    }

    protected void setCallback(RawRequestResult callback) {
        this.callback = callback;
    }

    protected Channel getChannel() {
        if(this.channel != null && this.channel.isOpen() && this.channel.isWritable()) {
            return this.channel;
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ClientChannelInitializer(new SimpleChannelInboundHandler() {
                @Override
                protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                    ByteBuf buf = (ByteBuf) o;
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    if (callback == null) {
                        logger.warn("No callback specified, dropping received data into oblivion.");
                    } else {
                        callback.respond(bytes);  //move parsing to pipeline
                    }
                }
            }));

            // Start client, wait for the connection and return the channel
            this.channel = b.connect(this.host, this.port).sync().channel();
            return this.channel;
        } catch (Exception e) { // InterruptedException and PRIVATE AnnotatedConnectException...
            logger.error("Unable to connect to Authentication Module: " + e.getMessage());
        }/*finally {
            workerGroup.shutdownGracefully();
        }*/
        return null;
    }

    protected void sendMessage(byte[] data) {
        // Get channel with the correct response handler
        Channel channel = this.getChannel();

        // Send data if possible
        if(channel != null) {
            channel.writeAndFlush(Unpooled.buffer().writeBytes(data));
        } else {
            throw new ChannelException("Unable to create channel.");
        }
    }
}
