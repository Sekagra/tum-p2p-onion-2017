package de.tum.in.net.group17.onion;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.onion.OnionCallback;
import de.tum.in.net.group17.onion.interfaces.onion.OnionInterface;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiCallback;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingInterface;
import de.tum.in.net.group17.onion.model.*;
import de.tum.in.net.group17.onion.parser.onionapi.OnionCoverParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelBuildParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDestroyParsedMessage;
import de.tum.in.net.group17.onion.parser.rps.RpsPeerParsedMessage;
import org.apache.log4j.Logger;

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

        // Listen for Onion connections and on the API
        this.onionInterface.setTunnel(tunnel);
        this.onionInterface.setSegments(segments);
        this.onionInterface.listen(getOnionCallback());

        // Listen for requests from the Calling Module
        this.apiInterface.listen(getOnionApiCallback());

        // Start with a delegate issuing the build of a random tunnel
        nextTunnelBuild = () -> buildTunnel();
    }

    /**
     * Retrieves a callback to handle messages arriving at the onion module that
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
            public void receviedDestroy(OnionTunnelDestroyParsedMessage msg) {
                // todo: Start a destruction sequence for a tunnel.
            }
        };
    }

    /**
     * Build a tunnel over several intermediate hops to a random destination.
     */
    private void buildTunnel() {
        // cover tunnels don't need IDs as they won't be addressed by them, but the destination is random
        this.rpsInterface.queryRandomPeer(result -> {
            Peer peer = Peer.fromRpsReponse((RpsPeerParsedMessage) result);
            buildTunnel(peer);
        });
    }

    /**
     * Build a tunnel over several intermediate hops to the given destination.
     * @param destination The peer that acts as a destination for the new tunnel.
     */
    private void buildTunnel(Peer destination) {
        Tunnel t = new Tunnel(this.tunnel.size());

        // get random intermediate hops to destination
        for (int i = 0; i < this.configProvider.getIntermediateHopCount(); i++) {
            // sync/async (order should be maintained on an open connection)?
            this.rpsInterface.queryRandomPeer(result -> {
                Peer peer = Peer.fromRpsReponse((RpsPeerParsedMessage) result);
                // add new segment and the peer instance as a reference to the hostkey is needed during the building phase
                t.addPeer(peer);
                t.addSegment(new TunnelSegment(LidImpl.createRandomLid(), peer.getIpAddress(), peer.getPort(), Direction.FORWARD));
            });
        }

        // add segment to segments list
        t.addPeer(destination);
        t.addSegment(new TunnelSegment(LidImpl.createRandomLid(), destination.getIpAddress(), destination.getPort(), Direction.FORWARD));

        // issue build
        this.onionInterface.buildTunnel(t);
    }

}
