package de.tum.in.net.group17.onion.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christoph Rudolf on 27.05.17.
 */
public class Tunnel {
    private int id;
    private List<TunnelSegment> segments;

    public Tunnel(int id) {
        this.id = id;
        this.segments = new ArrayList<>();
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
