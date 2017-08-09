package de.tum.in.net.group17.onion.interfaces;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.function.Supplier;

/**
 * Initializer for a Netty server parsing messages by their length field.
 * Created by Christoph Rudolf on 11.06.17.
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private Supplier<ChannelHandler> handlerSupplier;

    private final int WORKERS = 4;
    private EventExecutorGroup workerPool = new DefaultEventExecutorGroup(WORKERS);

    public ServerChannelInitializer(Supplier<ChannelHandler> handlerSupplier) {
        this.handlerSupplier = handlerSupplier;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("framer", new LengthFieldBasedFrameDecoder(65535, 0, 2, -2, 0));
        pipeline.addLast(workerPool, "handler", this.handlerSupplier.get());
    }
}
