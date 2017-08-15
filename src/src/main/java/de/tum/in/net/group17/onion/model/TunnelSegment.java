package de.tum.in.net.group17.onion.model;

import java.net.InetAddress;
import java.time.LocalDateTime;

/**
 * This class represents a segment of a tunnel as it is present for all cases in which a host is an intermediate hop.
 *
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

    /**
     * Create a new tunnel segment with the given parameters.
     *
     * @param lid The LID of the tunnel segment.
     * @param nextAddress The address of the next hop in the tunnel.
     * @param nextPort The port the next hop in the tunnel uses.
     * @param direction The direction: FORWARD => To tunnel endpoint; BACKWARD => To tunnel initiator.
     */
    public TunnelSegment(Lid lid, InetAddress nextAddress, short nextPort, Direction direction) {
        Lid = lid;
        this.nextAddress = nextAddress;
        this.nextPort = nextPort;
        this.direction = direction;

        updateLastDataSeen();
    }

    /**
     * Create a new tunnel segment with the given parameters.
     *
     * @param lid The LID of the tunnel segment.
     * @param peer The next hop in the tunnel (Wraps address, port and host key).
     * @param direction The direction: FORWARD => To tunnel endpoint; BACKWARD => To tunnel initiator.
     */
    public TunnelSegment(Lid lid, Peer peer, Direction direction) {
        this(lid, peer.getIpAddress(), peer.getPort(), direction);
        this.hostkey = peer.getHostkey();
    }

    /**
     * Get the LID identifying this segment.
     *
     * @return The LID.
     */
    public Lid getLid() {
        return Lid;
    }

    /**
     * Get the host key of the next hop in the tunnel.
     * Only valid if a Peer was used to create this object.
     *
     * @return The next hop's host key.
     */
    public byte[] getHostkey() {
        return hostkey;
    }

    /**
     * Get the segment linked to this tunnel segment.
     *
     * @return The linked tunnel segment.
     */
    public TunnelSegment getOther() {
        return other;
    }

    /**
     * Get the address of the next hop in the tunnel.
     *
     * @return The next hop's address.
     */
    public InetAddress getNextAddress() {
        return nextAddress;
    }

    /**
     * Get the port of the Onion module running on the next hop in the tunnel.
     *
     * @return The next hop's port.
     */
    public short getNextPort() {
        return nextPort;
    }

    /**
     * Get the session ID of the Onion Auth module linked to this segment.
     *
     * @return The session ID used by the Onion Auth module.
     */
    public short getSessionId() {
        return sessionId;
    }

    /**
     * Get the direction of this tunnel segment.
     * FORWARD => To tunnel endpoint; BACKWARD => To tunnel initiator.
     *
     * @return The direction of this segment.
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the time stamp we sent data thought this tunnel the last time.
     *
     * @return The time stamp we saw data the last time.
     */
    public LocalDateTime getLastDataSeen() {
        return this.lastDataSeen;
    }

    /**
     * Set the linked tunnel segment. (Segment in the other direction in the tunnel.)
     *
     * @param other The linked segment.
     */
    public void setOther(TunnelSegment other) {
        this.other = other;
    }

    /**
     * Set the session ID used by the Onion Auth module.
     *
     * @param sessionId The used session ID.
     */
    public void setSessionId(short sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Specify that we just sent data through the tunnel containing this segment.
     */
    public void updateLastDataSeen() {
        lastDataSeen = LocalDateTime.now();
    }
}
