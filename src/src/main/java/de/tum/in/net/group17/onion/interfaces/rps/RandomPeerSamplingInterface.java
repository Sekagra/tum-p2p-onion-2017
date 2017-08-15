package de.tum.in.net.group17.onion.interfaces.rps;

import de.tum.in.net.group17.onion.model.Peer;

import java.util.List;

/**
 * This interface is responsible for maintaining the connection to the RPS module.
 * It can be used to query new random peers and encapsulates the networking for such a query.
 * Created by Christoph Rudolf on 06.06.17.
 */
public interface RandomPeerSamplingInterface {
    /**
     * Query a new random peer from the RPS module.
     *
     *
     * @return The random peer.
     *
     * @throws RandomPeerSamplingException If we could not query a random peer.
     */
    Peer queryRandomPeer() throws RandomPeerSamplingException;

    /**
     * Query a new random peer from the RPS module but excluding certain host keys from being sampled. 10 retries.
     *
     *
     * @param excluding The list of all host keys that are not allowed to be used.
     *
     * @return The random peer.
     *
     * @throws RandomPeerSamplingException If we could not query a random peer.
     */
    Peer queryRandomPeer(List<String> excluding) throws RandomPeerSamplingException;
}
