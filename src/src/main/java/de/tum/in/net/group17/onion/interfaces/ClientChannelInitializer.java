package de.tum.in.net.group17.onion.interfaces;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


/**
 * Initializer for a Netty client parsing messages by their length field
 * Created by Christoph Rudolf on 06.06.17.
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private SimpleChannelInboundHandler handler;

    /**
     * Create a new ClientChannelInitializer.
     *
     * @param handler The last handler in the channel pipeline.
     */
    public ClientChannelInitializer(SimpleChannelInboundHandler handler) {
        this.handler = handler;
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("framer", new LengthFieldBasedFrameDecoder(65535, 0, 2, -2, 0));
        pipeline.addLast("handler", this.handler);
    }
}
