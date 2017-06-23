package de.tum.in.net.group17.onion.interfaces;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Base class for all interfaces needing a server running via UDP. This is currently only the case for the
 * Onion to Onion communication.
 * Provides a wrapping and basic handling of netty.
 * Created by Christoph Rudolf on 21.06.17.
 */
public class UdpServerInterfaceBase {

    /**
     * Start listening to incoming requests on the specified port for this server interface.
     *
     * @param port The port to be listening on.
     * @param handler The message handler for the unparsed but delimited message.
     */
    public void listen(final int port, final ChannelHandler handler) {
        final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("handler", handler);
                        }
                    });
            b.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
