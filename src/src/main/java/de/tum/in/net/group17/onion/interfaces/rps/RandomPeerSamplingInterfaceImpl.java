package de.tum.in.net.group17.onion.interfaces.rps;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParser;
import de.tum.in.net.group17.onion.parser.rps.RpsPeerParsedMessage;
import org.apache.log4j.Logger;
import sun.nio.ch.sctp.PeerAddrChange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Implementation of the RandomPeerSamplingInterface using Netty.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class RandomPeerSamplingInterfaceImpl extends TcpClientInterface implements RandomPeerSamplingInterface {
    private RandomPeerSamplingParser parser;
    private ConfigurationProvider config;
    private Logger logger;
    private List<Peer> peers;

    /**
     * Create a new RPS interface.
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received from the RPS module.
     */
    @Inject
    public RandomPeerSamplingInterfaceImpl(ConfigurationProvider config, RandomPeerSamplingParser parser) {
        super(config.getRpsModuleHost(), config.getRpsModulePort());
        this.logger = Logger.getLogger(RandomPeerSamplingInterface.class);
        this.parser = parser;
        this.config = config;
        this.peers = Collections.synchronizedList(new ArrayList<Peer>());
        setCallback(result -> randomPeerResult(result));
    }

    private void randomPeerResult(byte[] data) {
        try {
            peers.add(Peer.fromRpsReponse((RpsPeerParsedMessage) parser.parseMsg(data)));
            synchronized (peers) {
                peers.notify(); //Notify one consumer to be allowed to take a peer
            }
        } catch (ParsingException e) {
            logger.error("Parsing error for response: " + e.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    public Peer queryRandomPeer() throws RandomPeerSamplingException {
        // Build the query message
        ParsedMessage packet = null;
        try {
            packet = this.parser.buildRpsQueryMsg();
        } catch (ParsingException e) {
            throw new RandomPeerSamplingException("Parsing error during build: " + e.getMessage());
        }

        sendMessage(packet.serialize());

        try {
            synchronized (peers) {
                peers.wait(5000);
            }
        } catch (InterruptedException e) {
            throw new RandomPeerSamplingException("Interrupted during RPS fetch: " + e.getMessage());
        }

        if(!this.peers.isEmpty()) {
            return this.peers.get(0);
        } else {
            throw new RandomPeerSamplingException("No peer found despite of being notified of a peer.");
        }
    }
}
