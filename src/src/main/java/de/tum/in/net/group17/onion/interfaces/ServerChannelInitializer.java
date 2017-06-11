package de.tum.in.net.group17.onion.interfaces;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Initializer for a Netty server parsing messages by their length field.
 * Created by Christoph Rudolf on 11.06.17.
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private ChannelHandler handler;

    public ServerChannelInitializer(ChannelHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("framer", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 0));
        pipeline.addLast("handler", this.handler);
    }
}
