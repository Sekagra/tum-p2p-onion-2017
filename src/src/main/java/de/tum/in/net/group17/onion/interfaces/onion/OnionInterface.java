package de.tum.in.net.group17.onion.interfaces.onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.model.TunnelSegment;

import java.util.List;
import java.util.Map;

/**
 * This interface is responsible for serving incoming requests of fellow Onion modules and sending requests to them.
 * It is responsible for all Onion to Onion communication.
 * Created by Christoph Rudolf on 21.06.17.
 */
public interface OnionInterface {
    void listen(OnionCallback callback);

    void setTunnel(List<Tunnel> tunnel);
    void setSegments(Map<Lid, TunnelSegment> segments);

    void buildTunnel(Tunnel tunnel);
}
