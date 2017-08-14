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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.ini4j.InvalidFileFormatException;

import java.nio.file.NoSuchFileException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class orchestrates the flow of data between various interfaces.
 * It also acts as clock to manage rounds.
 * Created by Christoph Rudolf on 09.07.17.
 */
public class Orchestrator {
    protected int ROUND_START_DELAY = 3000;

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

    private Tunnel coverTunnel;

    /**
     * List of tunnels we received as an endpoint.
     */
    private Map<Integer, Tunnel> incomingTunnels;

    /**
     * List of segments, two for each time this hop is an intermediate hop.
     */
    private Map<Lid, TunnelSegment> segments;

    private static Logger logger = LogManager.getRootLogger();

    private TimerTask roundTask;
    private Timer roundTimer;

    public static void main(String[] args) {
        ConfigurationArguments config = null;

        try {
            // logpath == null: Solely the CONSOLE logger
            config = parseCommandLine(args);
        } catch(Exception e) {
            System.exit(1);
        }

        if(config == null) {
                    System.exit(0);
        }
        // Setting the log level in the root logger will set it in every child
        Configurator.setRootLevel(config.logLevel);

        logger.info("Starting up!");
        try {
            // Setup the dependency injection with Guice
            Injector injector = Guice.createInjector(new ProductionInjector(args[1]));
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

    private static ConfigurationArguments parseCommandLine(String[] arguments) throws IllegalArgumentException {
        String configPath = null, logPath = null;
        Level logLevel = Level.INFO;

        for(int i = 1; i < arguments.length; ++i) {
            switch(arguments[i]) {
                case "--help":
                    printHelp();
                    return null;
                case "--config":
                    configPath = arguments[++i];
                    break;
                case "--logfile":
                    logPath = arguments[++i];
                    break;
                case "loglevel":
                    try {
                    logLevel = Level.toLevel(arguments[i++]);
                    } catch(IllegalArgumentException e) {
                        System.err.println("Invalid log level: " + arguments[i]);
                        throw e;
                    }
                    break;
                default:
                    System.err.println("Invalid command line argument: " + arguments[i]);
                    throw new IllegalArgumentException();
            }
        }

        if(configPath == null) {
            System.err.println("Missing command line argument: config");
            printHelp();
            throw new MissingFormatArgumentException("config");
        }

        return new ConfigurationArguments(configPath, logPath, logLevel);
    }

    private static void printHelp() {
        String helpText =
                "Usage: java -jar onion_module.jar --config path [args]\n\n" +
                "\t--config\tpath\tPath to the configuration file.\n" +
                "\t--logfile\tpath\tFile used to output log messages.\n" +
                "\t--loglevel\tlevel\tLog level to use. Level = {debug, info, warning, error, fatal}\n\n";

        System.out.println(helpText);
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
        nextTunnelBuild = () -> setupCoverTunnel();

        roundTimer = new Timer();
        roundTask = getRoundTask();
        // wait a bit to give our RPS module that started along time to learn some hosts
        roundTimer.schedule(this.roundTask, ROUND_START_DELAY, this.configProvider.getRoundInterval().getSeconds() * 1000);
    }

    /**
     * Creates and returns a delegate that describes the functionality to be executed each round.
     * This is (in order):
     *      1. Tearing down the cover tunnel if a 'real' tunnel is to be build
     *      2. Refreshing old tunnels (including cover tunnel if one is there)
     *      3. Building a new tunnel as requested (cover or real)
     * @return The usable TimerTask instance with the described task.
     */
    private TimerTask getRoundTask() {
        return new TimerTask() {
            @Override
            public void run() {
                // Teardown the cover tunnel if a real tunnel is being build this round
                if(nextTunnelBuild != null && coverTunnel != null) {
                    try {
                        onionInterface.destroyTunnelById(coverTunnel.getId());
                        coverTunnel = null;
                    } catch (OnionException e) {
                        logger.warn("Error during cover tunnel teardown: " + e.getMessage());
                    }
                }

                refreshTunnels();

                // Build a new tunnel after refreshing old ones
                if(nextTunnelBuild != null) {
                    nextTunnelBuild.run();
                }

                cleanupOldStates();
            }
        };
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
                    logger.error("Unable to send incoming voice data to listening CM, have to drop them because: " + e.getMessage());
                }
            }

            @Override
            public void tunnelIncoming(TunnelSegment segment) {
                int tunnelId = getNextTunnelId();
                try {
                    apiInterface.sendIncoming(tunnelId);
                    Tunnel tunnel = new Tunnel(tunnelId);
                    tunnel.addSegment(segment);
                    incomingTunnels.put(tunnelId, tunnel);
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
                nextTunnelBuild = () -> setupVoiceTunnel(Peer.fromOnionBuild(msg));
            }

            @Override
            public void receivedCoverData(OnionCoverParsedMessage msg) {
                try {
                    onionInterface.sendCoverData(msg);
                } catch (OnionException e) {
                    logger.error("Cannot send cover data on tunnel! It is not possible to send an ONION ERROR as CM " +
                            "did not send us the tunnel we should send cover traffic on... Error: " + e.getMessage());
                }
            }

            @Override
            public void receivedVoiceData(OnionTunnelDataParsedMessage msg) {
                try {
                    onionInterface.sendVoiceData(msg);
                } catch (OnionException e) {
                    logger.error("Cannot send data received by CM on tunnel: " + msg.getTunnelId() +
                            "; Error: " + e.getMessage());
                    try {
                        apiInterface.sendError(msg.getTunnelId(), msg.getType());
                    } catch (OnionApiException e1) {
                        logger.error("Cannot even send error to CM module (disconnected?): " + e1.getMessage());
                    }
                }
            }

            @Override
            public void receivedDestroy(OnionTunnelDestroyParsedMessage msg) {
                try {
                    onionInterface.destroyTunnelById(msg.getTunnelId());
                } catch (OnionException e) {
                    try {
                        apiInterface.sendError(msg.getTunnelId(), MessageType.ONION_TUNNEL_DESTROY);
                    } catch (OnionApiException e1) {
                        logger.error("Cannot even send error to CM module (disconnected?): " + e1.getMessage());
                    }
                }
                // Clean up of tunnels
                startedTunnels.remove(msg.getTunnelId());
                incomingTunnels.remove(msg.getTunnelId());

                // todo: Specification doesn't say whether a cover tunnel has to be built if a voice tunnel is closed mid-round
                // Issue new cover tunnel build for new round
                if(startedTunnels.isEmpty()) {
                    nextTunnelBuild = () -> setupCoverTunnel();
                }
            }
        };
    }

    /**
     * Setup a tunnel over several intermediate hops to a random destination (COVER TUNNEL).
     */
    private void setupCoverTunnel() {
        // cover tunnels don't need IDs as they won't be addressed by them, but the destination is random
        Peer peer;
        try {
            peer = this.rpsInterface.queryRandomPeer();
        } catch (RandomPeerSamplingException e) {
            logger.error("Unable to get random peer as a cover tunnel destination: " + e.getMessage() + "\nRetry next round.");
            return;
        }
        try {
            this.coverTunnel = setupTunnel(peer);
        } catch (Exception e) {
            this.logger.error("Unable to setup cover tunnel, retry next round.");
        }
    }

    /**
     * Setup a tunnel over several intermediate hops to a requested destination (REQUESTED TUNNEL).
     * @param destination The peer that has been requested to act as a destination for the new tunnel.
     */
    private void setupVoiceTunnel(Peer destination) {
        try {
            Tunnel t = setupTunnel(destination);

            // Notify CM
            try {
                apiInterface.sendReady(t.getId(), destination.getHostkey());
            } catch (OnionApiException e) {
                logger.error("Error when notifying calling module of completed tunnel creation: " + e.getMessage());
                try {   // destroy tunnel if we cannot send READY message
                    onionInterface.destroyTunnelById(t.getId());
                } catch (OnionException e1) {
                    logger.error("Unable to teardown tunnel that is destroyed because we couldn't send a READY message to CM: " + e.getMessage());
                } finally {
                    startedTunnels.remove(t.getId());
                }
            }
        } catch (Exception e) {
            this.logger.error("Unable to setup voice tunnel, notify CM.");
            if(startedTunnels.isEmpty()) {
                nextTunnelBuild = () -> setupCoverTunnel();
            }
            try {
                // There is no tunnel created => no tunnel ID
                apiInterface.sendError(getNextTunnelId(), MessageType.ONION_TUNNEL_BUILD);
            } catch (OnionApiException e1) {
                logger.error("Cannot even send error to CM module (disconnected?): " + e1.getMessage());
            }
        }
    }

    /**
     * Setup a tunnel over several intermediate hops to the given destination.
     * @param destination The peer that acts as a destination for the new tunnel.
     */
    private Tunnel setupTunnel(Peer destination) throws RandomPeerSamplingException, InterruptedException, OnionException {
        Tunnel t = new Tunnel(getNextTunnelId());
        this.startedTunnels.put(t.getId(), t);

        try {
            buildTunnel(t, destination);
        } catch (RandomPeerSamplingException e) {
            this.startedTunnels.remove(t.getId());  // clear state
            this.logger.error("Unable to build tunnel due to lack of enough random peers: " + e.getMessage());
            throw e;    // throw further for eventual error to CM
        } catch (InterruptedException e) {
            this.startedTunnels.remove(t.getId());
            this.logger.error("Unable build, interrupted while waiting for response: " + e.getMessage());
            throw e;
        } catch (OnionException e) {
            this.logger.error("Unable build, error during extend: " + e.getMessage());
            this.onionInterface.destroyTunnelById(t.getId());
            throw e;
        }

        try {
            this.onionInterface.sendEstablished(t);
            Thread.sleep(333);
        } catch (OnionException e) {
            this.logger.error("Error when sending the final established message over the tunnel. Tunnel is not finished! Error: " + e.getMessage());
            this.onionInterface.destroyTunnelById(t.getId());
            throw e;
        } catch (InterruptedException e) {
            this.logger.warn("Interrupted while timeout after sending established!");
        }

        this.nextTunnelBuild = null;
        return t;
    }

    /**
     * Concrete building into a currently empty Tunnel data structure.
     * @param t The tunnel to build into.
     * @param destination The peer that acts as a destination for the new tunnel.
     */
    private void buildTunnel(Tunnel t, Peer destination) throws RandomPeerSamplingException, OnionException, InterruptedException {
        // get random intermediate hops to destination
        for (int i = 0; i < this.configProvider.getIntermediateHopCount(); i++) {
            Peer p = this.rpsInterface.queryRandomPeer();    // sync'd method
            this.onionInterface.extendTunnel(t, p);
        }

        this.onionInterface.extendTunnel(t, destination);
    }

    /**
     * Refresh all existing previously started tunnels for the new round.
     */
    public void refreshTunnels() {
        for (Tunnel t : this.startedTunnels.values()) {
            Tunnel tunnel = new Tunnel(t.getId());
            // Add the new tunnel with a temporal tunnelId to the started tunnels (necessary for creation)
            // We will remove the temporalId afterwards
            int temporalTunnelId = getNextTunnelId();
            this.startedTunnels.put(temporalTunnelId, tunnel);

            // We build this tunnel currently
            if(t.getSegments().isEmpty()) {
                continue;
            }

            // build new Tunnel with same tunnel ID and last peer as the old one
            TunnelSegment segment = t.getSegments().get(t.getSegments().size() - 1);
            try {
                buildTunnel(tunnel, new Peer(segment.getHostkey(), segment.getNextAddress(), segment.getNextPort()));
            } catch (RandomPeerSamplingException e) {
                logger.error("Unable to rebuild tunnel due to lack of enough random peers: " + e.getMessage() + "\nRetry next round.");
                // todo: Unspecified whether or not the tunnel should continue for another round in this case
                return;
            } catch (InterruptedException e) {
                logger.error("Unable to rebuild tunnel due to interrupted waiting: " + e.getMessage() + "\nRetry next round.");
                return;
            } catch (OnionException e) {
                logger.error("Unable to rebuild tunnel due to P2P error: " + e.getMessage() + "\nRetry next round.");
                return;
            }

            // Remove the temporal tunnelId from the startedTunnels map
            startedTunnels.remove(temporalTunnelId);
            // replace the tunnel instance in the startedTunnels map
            this.startedTunnels.put(t.getId(), tunnel);

            // send established with two local identifiers to mark switching process
            try {
                this.onionInterface.sendEstablished(tunnel, t);
            } catch (OnionException e) {
                this.logger.error("Error when sending the established message to mark the refresh of a tunnel: " + e.getMessage());
                this.startedTunnels.remove(t.getId());
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

    private void cleanupOldStates() {
        // Clean all segments for which we are an intermediate hop
        for(Lid lid : this.segments.keySet()) {
            if(Duration.between(segments.get(lid).getLastDataSeen(), LocalDateTime.now()).
                    compareTo(configProvider.getRoundInterval()) > 0) {
                // No data seen for a whole round
                segments.remove(lid);
            }
        }

        for(Integer tunnelId : this.incomingTunnels.keySet()) {
            if(incomingTunnels.get(tunnelId).getSegments().isEmpty()) {
                incomingTunnels.remove(tunnelId);
            } else if(Duration.between(incomingTunnels.get(tunnelId).getSegments().get(0).getLastDataSeen(),
                    LocalDateTime.now()).compareTo(configProvider.getRoundInterval()) > 0) {
                // No data seen in tunnel for one whole round
                incomingTunnels.remove(tunnelId);
            }
        }
    }


    /**
     * This class is used to return the command line arguments after parsing.
     */
    private static class ConfigurationArguments {
        public String configPath;
        public String logPath;
        public Level logLevel;

        public ConfigurationArguments(String configPath, String logPath, Level logLevel) {
            this.configPath = configPath;
            this.logPath = logPath;
            this.logLevel = logLevel;
        }
    }
}
