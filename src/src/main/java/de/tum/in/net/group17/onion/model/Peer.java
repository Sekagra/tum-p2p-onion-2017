package de.tum.in.net.group17.onion.model;

import de.tum.in.net.group17.onion.util.Hashing;

/**
 * Models a peer in the P2P network.
 * Created by Christoph Rudolf on 27.05.17.
 */
public class Peer {
    private String id;
    private byte[] hostkey;

    public Peer(byte[] hostkey) {
        this.hostkey = hostkey;
        this.id = Hashing.Sha256(hostkey);
    }

    public String getId() {
        return id;
    }

    public byte[] getHostkey() {
        return hostkey;
    }
}
