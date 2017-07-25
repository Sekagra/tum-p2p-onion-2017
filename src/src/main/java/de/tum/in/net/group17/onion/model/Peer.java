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

    public Peer() { };

    public Peer(byte[] hostkey)  {
        this.hostkey = hostkey;
    }

    public Peer(byte[] hostkey, InetAddress addr, short port)  {
        this.hostkey = hostkey;
        this.ipAddress = addr;
        this.port = port;
        this.id = Hashing.Sha256(hostkey);
    }

    public String getId() {
        return id;
    }

    public byte[] getHostkey() {
        return hostkey;
    }

    public InetAddress getIpAddress() {
        return this.ipAddress;
    }

    public short getPort() {
        return this.port;
    }

    public static Peer fromRpsReponse(RpsPeerParsedMessage msg) {
        try {
            return new Peer(msg.getKey().getEncoded("DER"), msg.getAddress(), msg.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Peer fromOnionBuild(OnionTunnelBuildParsedMessage msg) {
        try {
            return new Peer(msg.getDestinationKey().getEncoded("DER"), msg.getIpAddress(), msg.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
