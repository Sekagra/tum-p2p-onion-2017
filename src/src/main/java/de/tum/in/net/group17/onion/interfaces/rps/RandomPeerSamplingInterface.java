package de.tum.in.net.group17.onion.interfaces.rps;

import de.tum.in.net.group17.onion.model.Peer;

/**
 * This interface is responsible for maintaining the connection to the RPS module.
 * It can be used to query new random peers and encapsulates the networking for such a query.
 * Created by Christoph Rudolf on 06.06.17.
 */
public interface RandomPeerSamplingInterface {
    /**
     * Query a new random peer from the RPS module.
     * @return The random peer.
     */
    Peer queryRandomPeer() throws RandomPeerSamplingException;
}
