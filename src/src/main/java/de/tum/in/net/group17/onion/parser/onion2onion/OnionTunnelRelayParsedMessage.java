package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.06.17.
 *
 * This class represents a ONION_TUNNEL_REPLAY message in the first version of the application.
 * Objects of this class may only be created by a OnionToOnion parser after checking all parameters.
 */
public class OnionTunnelRelayParsedMessage extends OnionToOnionParsedMessage {
    private byte[] data; // Either the encrypted BLOB contained in the relay message or the data for the new hop

    private final Lid outgoingTunnel; // This LID of the tunnel we shall send the data to
    private final InetAddress address; // Address of the hop we shall forward the data to
    private final short port; // The port the next hop listens on
    private final boolean isIpv4;

    /**
     * Create a new unencrypted ONION_TUNNEL_RELAY message.
     * This object may only be created by a OnionToOnionParser after checking all parameters.
     *
     * @param incoming_tunnel The LID of the tunnel this message is received on from the receiving hosts perspective.
     * @param outgoingTunnel The LID of the new tunnel build by this relay message.
     * @param address The address of the new hop.
     * @param port The port the new hop listens on.
     * @param data The data contained in the relay message.
     */
    OnionTunnelRelayParsedMessage(Lid incoming_tunnel,
                                  Lid outgoingTunnel,
                                  InetAddress address,
                                  short port,
                                  byte[] data) {
        super(incoming_tunnel);
        this.isIpv4 = address instanceof Inet4Address;

        this.outgoingTunnel = outgoingTunnel;
        this.address = address;
        this.port = port;
        this.data = data;
    }


    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(this.getSize());
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put(super.serializeBase());
        buffer.put(address.getAddress());
        buffer.putShort(port);
        buffer.putShort((short)(isIpv4 ? 0 : 1 << 15));
        buffer.put(outgoingTunnel.serialize());
        buffer.put(data);

        return buffer.array();
    }

    /**
     * Request if this message contains an IPv4 or IPv6 address.
     *
     * @return True if this message contains an IPv4 address, false if IPv6.
     */
    public boolean isIpv4() {
        return isIpv4;
    }

    /**
     * Return the LID of the newly build tunnel.
     *
     * @return Lid object representing the LID of the new tunnel.
     */
    public Lid getOutgoingTunnel() {
        return outgoingTunnel;
    }

    /**
     * Return the address of the next hop in the tunnel.
     *
     * @return The IP address of the next hop.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Return the port the next hop listens on for Onion connections.
     *
     * @return The port the next hop listens on.
     */
    public short getPort() {
        return port;
    }

    /**
     * Get the data contained in this message.
     *
     * @return A byte[] representing the data contained in this message.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short) (getSizeBase() +
                4 + (isIpv4 ? 4 : 16) +
                outgoingTunnel.getSize() +
                data.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_RELAY;
    }
}
