package de.tum.in.net.group17.onion.interfaces.onionapi;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.ServerInterfaceBase;
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
public class OnionApiInterfaceImpl extends ServerInterfaceBase implements OnionApiInterface {
    private OnionApiParser parser;
    private ConfigurationProvider config;

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
        };
    }
}
