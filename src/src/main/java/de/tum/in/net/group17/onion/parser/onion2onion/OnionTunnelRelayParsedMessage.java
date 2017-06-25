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
    private boolean isEncrypted; // Does this message contain encrypted data?
    private byte[] data; // Either the encrypted BLOB contained in the relay message or the data for the new hop

    // Following member variables are only valid if the Relay Message is not encrypted
    private Lid outgoingTunnel; // This LID of the tunnel we shall send the data to
    private InetAddress address; // Address of the hop we shall forward the data to
    private short port; // The port the next hop listens on
    private boolean isIpv4;

    /**
     * Create a new ONION_TUNNEL_RELAY message containing encrypted data.
     * This message may only be created by a OnionToOnionParser after checking all parameters.
     *
     * @param incoming_tunnel The LID of the tunnel we received this message on.
     * @param data The encrypted data contained in the message.
     */
    OnionTunnelRelayParsedMessage(Lid incoming_tunnel, byte[] data) {
        super(incoming_tunnel);
        this.data = data;
    }

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
        this.isEncrypted = false;
        this.isEncrypted = address instanceof Inet4Address;

        this.outgoingTunnel = outgoingTunnel;
        this.address = address;
        this.port = port;
        this.data = data;
    }

    /**
     * Set this message to 'encrypted' state and set the encrypted data.
     * This method may only be called by a OnionToOnionParser.
     *
     * @param data The encrypted data.
     */
    void setEncryptedData(byte[] data) {
        isEncrypted = true;
        this.data = data;
    }

    /**
     * Set this message to 'unencrypted' state and set the unencrypted information.
     * This message method may only be called by a OnionToOnionParser.
     *
     * @param outgoing_tunnel The LID of the new tunnel build by this relay message.
     * @param address The address of the new hop.
     * @param port The port the new hop listens on.
     * @param data The data contained in the relay message.
     */
    void setUnencryptedData(Lid outgoing_tunnel, InetAddress address, short port, byte[] data) {
        isEncrypted = false;
        isIpv4 = address instanceof Inet4Address;

        this.outgoingTunnel = outgoing_tunnel;
        this.address = address;
        this.port = port;
        this.data = data;
    }

    /**
     * Get the data contained in this unencrypted message.
     * The returned BLOB contains the information which needs to be encrypted to generate a encrypted ONION_TUNNEL_RELAY
     * message.
     * It is only valid to call this method in 'unencrypted' state.
     *
     * @returns A byte[] containing the outgoingTunnel LID, IP address, port and the data. This information has to be
     *          encrypted for sending.
     */
    public byte[] getUnencryptedBlob() {
        if(isEncrypted)
            throw new IllegalStateException("Cannot retrieve this information from encrypted data!");

        ByteBuffer buffer = ByteBuffer.allocate(this.getSize());
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put(outgoingTunnel.serialize());
        buffer.put(address.getAddress());
        buffer.putShort(port);
        buffer.putShort((short)0);
        buffer.put(data);

        return buffer.array();
    }

    /**
     * Request if this message is in 'encrypted' or 'unencrypted' state.
     *
     * @return Boolean indicating the encryption state.
     */
    public boolean isEncrypted() {
        return isEncrypted;
    }

    /**
     * Request if this message contains an IPv4 or IPv6 address.
     * It is only valid to call this method in 'unencrypted' state. If called in 'encrypted' state a
     * IllegalStateException is thrown.
     *
     * @return True if this message contains an IPv4 address, false if IPv6.
     */
    public boolean isIpv4() {
        if(isEncrypted)
            throw new IllegalStateException("Cannot retrieve the IP address from encrypted data!");
        return isIpv4;
    }

    /**
     * Return the LID of the newly build tunnel.
     * It is only valid to call this method in 'unencrypted' state. If called in 'encrypted' state a
     * IllegalStateException is thrown.
     *
     * @return Lid object representing the LID of the new tunnel.
     */
    public Lid getOutgoingTunnel() {
        if(isEncrypted)
            throw new IllegalStateException("Cannot retrieve the outgoing tunnel LID from encrypted data!");
        return outgoingTunnel;
    }

    /**
     * Return the address of the next hop in the tunnel.
     * It is only valid to call this method in 'unencrypted' state. If called in 'encrypted' state a
     * IllegalStateException is thrown.
     *
     * @return The IP address of the next hop.
     */
    public InetAddress getAddress() {
        if(isEncrypted)
            throw new IllegalStateException("Cannot retrieve the address of the next hop from encrypted data!");
        return address;
    }

    /**
     * Return the port the next hop listens on for Onion connections.
     * Is is only valid to call this method in 'unencrypted' state. If called in 'encrypted' state a
     * IllegalStateException is thrown.
     *
     * @return The port the next hop listens on.
     */
    public short getPort() {
        if(isEncrypted)
            throw new IllegalStateException("Cannot retrieve the port the next hop listens on from encrypted data");
        return port;
    }

    /**
     * Get the data contained in this message.
     * If in 'encrypted' state, data is the encrypted BLOB retrieved from the initiator or sent to the next hop.
     * If in 'unencrypted' state, data is the message sent to the next hop.
     *
     * @return A byte[] representing the data contained in this message.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @inheritDoc
     *
     * Serialization is only valid if encrypted data is stored in the object.
     * If unencrypted data is stored a IllegalStateException is thrown!
     */
    public byte[] serialize() {
        if(!isEncrypted)
            throw new IllegalStateException("Cannot serialize unencrypted data!");

        ByteBuffer buffer = ByteBuffer.wrap(super.serializeBase()); // TODO: Change message format and include port, res in the encrypted data
        buffer.put(data);
        return buffer.array();
    }

    /**
     * Get the size of this message.
     * If the object stores encrypted data, this is the size of the whole message to send.
     *
     * If we store unencrypted data, this is the size of the data that needs to be encrypted!
     *
     * @returns The size of this message.
     */
    public short getSize() {
        if(isEncrypted)
            return (short)(super.getSizeBase() + data.length);
        return (short)(outgoingTunnel.getSize() + (isIpv4 ? 4 : 16) + 4 + data.length); // outgoingLid; ip address; port + res(4); data
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_RELAY;
    }
}
