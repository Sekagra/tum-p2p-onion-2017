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
    /**
     * Send out a single UDP datagram message to the specified receiver.
     * @param targetIp The IP to send the message to.
     * @param targetPort The targeted port on the receivers end.
     * @param data The raw data to send to.
     * @throws IOException
     */
    public void send(InetAddress targetIp, int targetPort, byte[] data) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket packet = new DatagramPacket(data, data.length, targetIp, targetPort);
        socket.send(packet);
    }
}
