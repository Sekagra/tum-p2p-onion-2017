package de.tum.in.net.group17.onion.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christoph Rudolf on 27.05.17.
 */
public class Tunnel {
    private int id;
    private List<TunnelSegment> segments;

    /**
     * Create a new tunnel with the given tunnel ID.
     *
     * @param id The tunnel ID of this tunnel.
     */
    public Tunnel(int id) {
        this.id = id;
        this.segments = new ArrayList<>();
    }

    /**
     * Retrieve the ID of this tunnel.
     * @return The ID of this tunnel.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Retrieve the list of segments of this tunnel.
     * @return The list of segments inside this tunnel.
     */
    public List<TunnelSegment> getSegments() {
        return this.segments;
    }

    /**
     * Add a new TunnelSegment to this tunnel, advancing it by one hop.
     * The new segment will mark the endpoint for this tunnel until another one is added.
     * @param segment The segment to add to this tunnel.
     */
    public void addSegment(TunnelSegment segment) {
        this.segments.add(segment);
    }
}
