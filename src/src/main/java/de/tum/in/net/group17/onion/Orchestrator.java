package de.tum.in.net.group17.onion;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.onion.OnionCallback;
import de.tum.in.net.group17.onion.interfaces.onion.OnionException;
import de.tum.in.net.group17.onion.interfaces.onion.OnionInterface;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiCallback;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingException;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingInterface;
import de.tum.in.net.group17.onion.model.*;
import de.tum.in.net.group17.onion.parser.onionapi.OnionCoverParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelBuildParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDestroyParsedMessage;
import de.tum.in.net.group17.onion.parser.rps.RpsPeerParsedMessage;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * List of tunnels this peer has started.
     */
    private List<Tunnel> tunnel;

    /**
     * List of segments, two for each time this hop is an intermediate hop.
     */
    private Map<Lid, TunnelSegment> segments;

    private static Logger logger = Logger.getRootLogger();

    public static void main(String[] args) {
        logger.info("Starting up!");

        // Setup the dependency injection with Guice
        Injector injector = Guice.createInjector(new ProductionInjector());
        Orchestrator orchestrator = injector.getInstance(Orchestrator.class);
        orchestrator.start();
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
        this.tunnel = new ArrayList<>();
        this.segments = new HashMap<Lid, TunnelSegment>();

        // Listen for Onion connections and on the API
        this.onionInterface.setTunnel(tunnel);
        this.onionInterface.setSegments(segments);
        this.onionInterface.listen(getOnionCallback());

        // Listen for requests from the Calling Module
        this.apiInterface.listen(getOnionApiCallback());

        // Start with a delegate issuing the build of a random tunnel
        nextTunnelBuild = () -> buildTunnel();

        buildTunnel();


    }

    /**
     * Retrieves a callback to handle messages arriving at the onion2onion module that
     * have to be given or reacted to by any other module.
     * @return A OnionCallback instance with the methods for reacting to the specific events.
     */
    private OnionCallback getOnionCallback() {
        return new OnionCallback() {
            @Override
            public void tunnelAccepted(int tunnelId) {
                // todo: Notify the CM via "ONION TUNNEL READY"
            }

            @Override
            public void error() {
                // todo: Notify the CM via "ONION ERROR"
            }

            @Override
            public void tunnelData(int tunnelId, OnionTunnelDataParsedMessage msg) {
                // todo: Notify the CM via "ONION TUNNEL DATA"
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
                nextTunnelBuild = () -> buildTunnel(Peer.fromOnionBuild(msg));
            }

            @Override
            public void receivedCoverData(OnionCoverParsedMessage msg) {
                // todo: Data to forward over a tunnel.
            }

            @Override
            public void receivedVoiceData(OnionTunnelDataParsedMessage msg) {
                // todo: Cover traffic instruction
            }

            @Override
            public void receivedDestroy(OnionTunnelDestroyParsedMessage msg) {
                // todo: Start a destruction sequence for a tunnel.
            }
        };
    }

    /**
     * Build a tunnel over several intermediate hops to a random destination.
     */
    private void buildTunnel() {
        // cover tunnels don't need IDs as they won't be addressed by them, but the destination is random
        try {
            Peer peer = this.rpsInterface.queryRandomPeer();
            buildTunnel(peer);
        } catch (RandomPeerSamplingException e) {
            logger.error("Unable to get random peer as cover tunnel destination: " + e.getMessage());
        }
    }

    /**
     * Build a tunnel over several intermediate hops to the given destination.
     * @param destination The peer that acts as a destination for the new tunnel.
     */
    private void buildTunnel(Peer destination) {
        Tunnel t = new Tunnel(this.tunnel.size());

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
            } catch (OnionException e) {
                logger.error("Unable to create tunnel: " + e.getMessage());
                return;
            }
        }

        this.tunnel.add(t);
    }

}
