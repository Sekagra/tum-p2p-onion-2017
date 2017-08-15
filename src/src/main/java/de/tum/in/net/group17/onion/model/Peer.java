package de.tum.in.net.group17.onion.model;

import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelBuildParsedMessage;
import de.tum.in.net.group17.onion.parser.rps.RpsPeerParsedMessage;
import de.tum.in.net.group17.onion.util.Hashing;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Models a peer in the P2P network.
 * Created by Christoph Rudolf on 27.05.17.
 */
public class Peer {
    private String id;
    private byte[] hostkey;
    private InetAddress ipAddress;
    private short port;

    /**
     * Create a new peer without host key, address and port.
     */
    public Peer() { };

    /**
     * Create a new peer with host key information.
     *
     * @param hostkey The host key of this peer.
     */
    public Peer(byte[] hostkey)  {
        this.hostkey = hostkey;
    }

    /**
     * Create a new peer.
     *
     * @param hostkey The host key of this peer.
     * @param addr The address of this peer.
     * @param port The port of this peer's application.
     */
    public Peer(byte[] hostkey, InetAddress addr, short port)  {
        this.hostkey = hostkey;
        this.ipAddress = addr;
        this.port = port;
        this.id = Hashing.Sha256(hostkey);
    }

    /**
     * Get the ID (Hash of the public key) of this peer.
     *
     * @return The host's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the host key of this peer.
     *
     * @return The peer's key.
     */
    public byte[] getHostkey() {
        return hostkey;
    }

    /**
     * Get the address of the peer.
     *
     * @return The peer's address.
     */
    public InetAddress getIpAddress() {
        return this.ipAddress;
    }

    /**
     * Get the port used by the peer's Onion module.
     *
     * @return The Onion module's port on the peer.
     */
    public short getPort() {
        return this.port;
    }

    /**
     * Create a peer from a RPS PEER message.
     *
     *
     * @param msg The message.
     *
     * @return The created peer.
     */
    public static Peer fromRpsReponse(RpsPeerParsedMessage msg) {
        try {
            return new Peer(msg.getKey().getEncoded("DER"), msg.getAddress(), msg.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Create a peer from an ONION TUNNEL BUILD message.
     *
     *
     * @param msg The message.
     *
     * @return The created peer.
     */
    public static Peer fromOnionBuild(OnionTunnelBuildParsedMessage msg) {
        try {
            return new Peer(msg.getDestinationKey().getEncoded("DER"), msg.getIpAddress(), msg.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
