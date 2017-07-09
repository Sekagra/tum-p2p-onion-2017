package de.tum.in.net.group17.onion.interfaces.onionapi;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.config.ConfigurationProviderImpl;
import de.tum.in.net.group17.onion.interfaces.TcpServerInterfaceBase;
import de.tum.in.net.group17.onion.parser.onionapi.OnionApiParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Implementation of the Onion API interface offering services to the UI/CM.
 * Created by Christoph Rudolf on 11.06.17.
 */
public class OnionApiInterfaceImpl extends TcpServerInterfaceBase implements OnionApiInterface {
    private OnionApiParser parser;
    private ConfigurationProvider config;

    /**
     * Create a new Onion API interface.
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received at the Onion API interface.
     */
    @Inject
    public OnionApiInterfaceImpl(ConfigurationProvider config, OnionApiParser parser) {
        this.parser = parser;
        this.config = config;
        this.port = this.config.getOnionApiPort();
    }

    protected ChannelHandler getHandler() {
        return new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                System.out.println(ctx.channel().remoteAddress().toString() + " has connected (Active).");
            }

            @Override
            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                super.channelRegistered(ctx);
                System.out.println(ctx.channel().remoteAddress().toString() + " has connected (Register).");
            }

            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                Channel channel = channelHandlerContext.channel();
                System.out.println(channel.remoteAddress().toString() + " sent us " + byteBuf.toString());
            }

            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
                System.out.println( "channelReadComplete ++++");
                ctx.fireChannelReadComplete();
            }

            @Override
            public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                super.channelUnregistered(ctx);
                System.out.println(ctx.channel().remoteAddress().toString() + " has disconnected (Unregister).");

            }
        };
    }

    @Override
    public void sendIncoming() {

    }
}
