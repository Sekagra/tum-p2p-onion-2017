package de.tum.in.net.group17.onion;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.onion.OnionCallback;
import de.tum.in.net.group17.onion.interfaces.onion.OnionException;
import de.tum.in.net.group17.onion.interfaces.onion.OnionInterface;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiCallback;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiException;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingException;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingInterface;
import de.tum.in.net.group17.onion.model.*;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.onionapi.OnionCoverParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelBuildParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDestroyParsedMessage;
import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;

import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class orchestrates the flow of data between various interfaces.
 * It also acts as clock to manage rounds.
 * Created by Christoph Rudolf on 09.07.17.
 */
public class Orchestrator {
    @Inject
    private RandomPeerSamplingInterface rpsInterface;
    @Inject
    private OnionApiInterface apiInterface;
    @Inject
    private OnionInterface onionInterface;
    @Inject
    private ConfigurationProvider configProvider;

    private Runnable nextTunnelBuild;

    private final AtomicInteger tunnelId = new AtomicInteger();

    /**
     * List of tunnels this peer has started.
     */
    private Map<Integer, Tunnel> startedTunnels;

    /**
     * List of tunnels we received as an endpoint.
     */
    private Map<Integer, TunnelSegment> incomingTunnels;

    /**
     * List of segments, two for each time this hop is an intermediate hop.
     */
    private Map<Lid, TunnelSegment> segments;

    private static Logger logger = Logger.getRootLogger();

    public static void main(String[] args) {
        logger.info("Starting up!");

        if(args.length < 0) {
            logger.fatal("No path for configuration file given as command line argument!");
            System.err.println("No path for configuration file given as command line argument!");
            System.exit(1);
        }

        try {
            // Setup the dependency injection with Guice
            Injector injector = Guice.createInjector(new ProductionInjector(args[0]));
            Orchestrator orchestrator = injector.getInstance(Orchestrator.class);
            orchestrator.start();
        } catch (NoSuchFileException e) {
            logger.fatal("Could not set up Onion module: " + e.getMessage());
            System.exit(1);
        }
        catch(InvalidFileFormatException e) {
            logger.fatal("Could not parse configuration file: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Starting to connect to all other interfaces and initiate the first Onion round
     */
    public void start() {
        // Test if injection worked and it is not null
        assert rpsInterface != null;
        assert apiInterface != null;
        assert onionInterface != null;
        assert configProvider != null;

        // Init data structures
        this.startedTunnels = new HashMap<>();
        this.incomingTunnels = new HashMap<>();
        this.segments = new HashMap<>();

        // Listen for Onion connections
        this.onionInterface.setTunnels(this.startedTunnels, this.incomingTunnels);
        this.onionInterface.setSegments(this.segments);
        this.onionInterface.listen(getOnionCallback());

        // Listen for requests from the Calling Module
        this.apiInterface.listen(getOnionApiCallback());

        // Start with a delegate issuing the build of a random tunnel
        nextTunnelBuild = () -> setupTunnel();
    }

    /**
     * Retrieves a callback to handle messages arriving at the onion2onion module that
     * have to be given or reacted to by any other module.
     * @return A OnionCallback instance with the methods for reacting to the specific events.
     */
    private OnionCallback getOnionCallback() {
        return new OnionCallback() {
            @Override
            public void error(int tunnelId, MessageType type) {
                try {
                    apiInterface.sendError(tunnelId, type);
                } catch (OnionApiException e) {
                    logger.error("Error while notifying the calling module of an error happening when serving one of its requests: " + e.getMessage());
                }
            }

            @Override
            public void tunnelData(int tunnelId, byte[] data) {
                try {
                    apiInterface.sendVoiceData(tunnelId, data); // Notify the CM via "ONION TUNNEL DATA"
                } catch (OnionApiException e) {
                    // todo: terminate tunnel?!
                }
            }

            @Override
            public void tunnelIncoming(TunnelSegment segment) {
                int tunnelId = getNextTunnelId();
                try {
                    apiInterface.sendIncoming(tunnelId);
                    incomingTunnels.put(tunnelId, segment);
                } catch (OnionApiException e) {
                    logger.error("Unable to send ONION TUNNEL INCOMING message to connect CM: " + e.getMessage());
                }
            }

            @Override
            public void tunnelDestroyed(int tunnelId) {
                startedTunnels.remove(tunnelId);
                incomingTunnels.remove(tunnelId);
            }
        };
    }

    /**
     * Retrieves a callback to handle messages arriving at the Onion API that
     * have to be given or reacted to by any other module.
     * @return A OnionApiCallback instance with methods for reacting to the specific events.
     */
    private OnionApiCallback getOnionApiCallback() {
        return new OnionApiCallback() {
            @Override
            public void receivedTunnelBuild(OnionTunnelBuildParsedMessage msg) {
                // Register a tunnel build to the given destination in the next round
                //nextTunnelBuild = () -> setupTunnel(Peer.fromOnionBuild(msg));
                setupTunnel(Peer.fromOnionBuild(msg));
                // TODO: Add round concept!
            }

            @Override
            public void receivedCoverData(OnionCoverParsedMessage msg) {
                onionInterface.sendCoverData(msg);
            }

            @Override
            public void receivedVoiceData(OnionTunnelDataParsedMessage msg) {
                onionInterface.sendVoiceData(msg);
            }

            @Override
            public void receivedDestroy(OnionTunnelDestroyParsedMessage msg) {
                onionInterface.destroyTunnel(msg.getTunnelId());
                // Clean up of tunnels
                startedTunnels.remove(msg.getTunnelId());
                incomingTunnels.remove(msg.getTunnelId());
            }
        };
    }

    /**
     * Setup a tunnel over several intermediate hops to a random destination.
     */
    private void setupTunnel() {
        // cover tunnels don't need IDs as they won't be addressed by them, but the destination is random
        try {
            Peer peer = this.rpsInterface.queryRandomPeer();
            setupTunnel(peer);
        } catch (RandomPeerSamplingException e) {
            logger.error("Unable to get random peer as cover tunnel destination: " + e.getMessage());
        }
    }

    /**
     * Setup a startedTunnels over several intermediate hops to the given destination.
     * @param destination The peer that acts as a destination for the new tunnel.
     */
    private void setupTunnel(Peer destination) {
        Tunnel t = new Tunnel(getNextTunnelId());
        this.startedTunnels.put(t.getId(), t);

        buildTunnel(t, destination);

        try {
            this.onionInterface.sendEstablished(t);
        } catch (OnionException e) {
            this.logger.error("Error when sending the final established message over the tunnel: " + e.getMessage());
            this.startedTunnels.remove(t);
            return;
        }

        try {
            this.apiInterface.sendReady(t.getId(), destination.getHostkey());
        } catch (OnionApiException e) {
            this.logger.error("Error when notifying calling module of completed tunnel creation: " + e.getMessage());
            this.startedTunnels.remove(t);
            return;
        }
    }

    /**
     * Concrete building into a currently empty Tunnel data structure.
     * @param t The tunnel to build into.
     * @param destination The peer that acts as a destination for the new tunnel.
     */
    private void buildTunnel(Tunnel t, Peer destination) {
        // get random intermediate hops to destination
        for (int i = 0; i < this.configProvider.getIntermediateHopCount(); i++) {
            Peer p = null;
            try {
                p = this.rpsInterface.queryRandomPeer();    // sync'd method
            } catch (RandomPeerSamplingException e) {
                logger.error("Unable to get random intermediate peer from RPS module: " + e.getMessage());
                //todo: write error to CM
            }
            try {
                this.onionInterface.extendTunnel(t, p);
            } catch (Exception e) {
                logger.error("Unable to create tunnel: " + e.getMessage());
                return;
            }
        }

        try {
            this.onionInterface.extendTunnel(t, destination);
        } catch (Exception e) {
            logger.error("Unable to create tunnel: " + e.getMessage());
            this.startedTunnels.remove(t);
            return;
        }
    }

    /**
     * Refresh all existing previously started tunnels for the new round.
     */
    public void refreshTunnels() {
        for (Tunnel t : this.startedTunnels.values()) {
            Tunnel tunnel = new Tunnel(t.getId());

            // build new Tunnel with same tunnel ID and last peer as the old one
            TunnelSegment segment = tunnel.getSegments().get(tunnel.getSegments().size() - 1);
            buildTunnel(tunnel, new Peer(segment.getHostkey(), segment.getNextAddress(), segment.getNextPort()));

            // replace the tunnel instance in the startedTunnels map
            this.startedTunnels.put(t.getId(), tunnel);

            // send established with two local identifiers to mark switching process
            try {
                this.onionInterface.sendEstablished(tunnel, t);
            } catch (OnionException e) {
                this.logger.error("Error when sending the established message to mark the refresh of a tunnel: " + e.getMessage());
                this.startedTunnels.remove(t);
                return;
            }
        }
    }

    /**
     * Get next tunnel ID from atomic integer.
     *
     * @return The next usable tunnel ID.
     */
    private int getNextTunnelId() {
        return tunnelId.getAndIncrement();
    }

}
