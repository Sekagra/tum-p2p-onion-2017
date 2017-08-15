package de.tum.in.net.group17.onion.interfaces;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 * Interface for message handlers used with UDP servers to completely separate an interface
 * using it from netty callbacks.
 * Created by Christoph Rudolf on 17.07.17.
 */
public interface UdpMessageHandler {
    /**
     * Create a new UDP handler.
     *
     * @param ctx The context of the message.
     * @param packet The incoming datagram.
     */
    void readDatagram(ChannelHandlerContext ctx, DatagramPacket packet);
}
