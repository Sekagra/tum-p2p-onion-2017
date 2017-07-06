package de.tum.in.net.group17.onion.interfaces;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A simple UDP client to send UDP packets. This implementation is independent of Netty as the pipeline structure is
 * unnecessary for a simple UDP client.
 * Created by Christoph Rudolf on 29.06.17.
 */
public class UdpClient {
    public void send(InetAddress targetIp, int targetPort, ParsedMessage data) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = data.serialize();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, targetIp, targetPort);
        socket.send(packet);
    }
}
