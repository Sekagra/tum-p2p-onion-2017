package de.tum.in.net.group17.onion.model;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * This class represents a segment of a tunnel as it is present for all cases in which a host is an intermediate hop.
 * Created by Christoph Rudolf on 13.07.17.
 */
public class TunnelSegment {
    private Lid Lid;
    private TunnelSegment other;    // the next TunnelSegment holding information for forwarding
    private InetAddress nextAddress;
    private short nextPort;
    private short sessionId;
    private Direction direction;
    private byte[] hostkey;
    private LocalDateTime lastDataSeen;

    public TunnelSegment(Lid lid, InetAddress nextAddress, short nextPort, Direction direction) {
        Lid = lid;
        this.nextAddress = nextAddress;
        this.nextPort = nextPort;
        this.direction = direction;
    }

    public TunnelSegment(Lid lid, Peer peer, Direction direction) {
        this(lid, peer.getIpAddress(), peer.getPort(), direction);
        this.hostkey = peer.getHostkey();

        updateLastDataSeen();
    }

    public de.tum.in.net.group17.onion.model.Lid getLid() {
        return Lid;
    }

    public byte[] getHostkey() {
        return hostkey;
    }

    public TunnelSegment getOther() {
        return other;
    }

    public InetAddress getNextAddress() {
        return nextAddress;
    }

    public short getNextPort() {
        return nextPort;
    }

    public short getSessionId() {
        return sessionId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setOther(TunnelSegment other) {
        this.other = other;
    }

    public void setSessionId(short sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getLastDataSeen() {
        return this.lastDataSeen;
    }

    public void updateLastDataSeen() {
        lastDataSeen = LocalDateTime.now();
    }
}
