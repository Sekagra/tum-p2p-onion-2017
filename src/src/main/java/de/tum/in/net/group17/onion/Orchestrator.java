package de.tum.in.net.group17.onion;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.tum.in.net.group17.onion.interfaces.onion.OnionCallback;
import de.tum.in.net.group17.onion.interfaces.onion.OnionInterface;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiCallback;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingInterface;
import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.parser.onionapi.OnionCoverParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelBuildParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDestroyParsedMessage;

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

    /**
     * List of tunnels this peer has started.
     */
    private List<Tunnel> tunnel;

    /**
     * List of segments, two for each time this hop is an intermediate hop.
     */
    private Map<Lid, TunnelSegment> segments;

    public static void main(String[] args) {
        System.out.println("Starting up!");

        // Setup the dependency injection with Guice
        Injector injector = Guice.createInjector(new ProductionInjector());
        Orchestrator orchestrator = injector.getInstance(Orchestrator.class);
        orchestrator.start();
    }

    public void start() {
        // Test if injection worked and it is not null
        System.out.println(rpsInterface.toString());
        System.out.println(apiInterface.toString());
        System.out.println(onionInterface.toString());

        // Listen for Onion connections and on the API
        this.onionInterface.setTunnel(tunnel);
        this.onionInterface.setSegments(segments);
        this.onionInterface.listen(getOnionCallback());

        // Listen for requests from the Calling Module
        this.apiInterface.listen(getOnionApiCallback());
    }

    /**
     * Retrieves a callback to handle messages arriving at the onion module that
     * have to been given to other modules.
     * @return A OnionCallback instance with the methods for reacting to implemented.
     */
    private OnionCallback getOnionCallback() {
        return new OnionCallback() {
            @Override
            public void tunnelAccepted(int tunnelId) {
                // Notify the CM via "ONION TUNNEL READY"
            }

            @Override
            public void error() {
                // Notify the CM via "ONION ERROR"
            }

            @Override
            public void tunnelData(int tunnelId, OnionTunnelDataParsedMessage msg) {
                // Notify the CM via "ONION TUNNEL DATA"
            }
        };
    }

    private OnionApiCallback getOnionApiCallback() {
        return new OnionApiCallback() {
            @Override
            public void receivedTunnelBuild(OnionTunnelBuildParsedMessage msg) {
                // Start a new tunnel building sequence with the Onion module.
            }

            @Override
            public void receivedCoverData(OnionCoverParsedMessage msg) {
                // Data to forward over a tunnel.
            }

            @Override
            public void receivedVoiceData(OnionTunnelDataParsedMessage msg) {
                // Cover traffic instruction
            }

            @Override
            public void receviedDestroy(OnionTunnelDestroyParsedMessage msg) {
                // Start a destruction sequence for a tunnel.
            }
        };
    }

}
