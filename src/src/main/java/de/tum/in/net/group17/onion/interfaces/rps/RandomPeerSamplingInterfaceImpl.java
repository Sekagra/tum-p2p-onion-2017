package de.tum.in.net.group17.onion.interfaces.rps;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParser;
import de.tum.in.net.group17.onion.parser.rps.RpsPeerParsedMessage;
import io.netty.channel.ChannelException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
        super(config.getRpsApiHost(), config.getRpsApiPort());
        this.logger = LogManager.getLogger(RandomPeerSamplingInterface.class);
        this.parser = parser;
        this.config = config;
        this.peers = Collections.synchronizedList(new ArrayList<Peer>());
        setCallback(result -> randomPeerResult(result));
    }

    /**
     * Triggered when a new random peer is being delivered by the RPS module.
     *
     * @param data The raw data from RPS.
     */
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
    @Override
    public Peer queryRandomPeer() throws RandomPeerSamplingException {
        // Build the query message
        ParsedMessage packet = null;
        try {
            packet = this.parser.buildRpsQueryMsg();
        } catch (ParsingException e) {
            throw new RandomPeerSamplingException("Parsing error during build: " + e.getMessage());
        }

        try {
            sendMessage(packet.serialize());
        } catch(ChannelException e) { // No connection to RPS
            throw new RandomPeerSamplingException("Unable to fetch random peer: " + e.getMessage());
        }

        try {
            synchronized (peers) {
                peers.wait(5000);
            }
        } catch (InterruptedException e) {
            throw new RandomPeerSamplingException("Interrupted during RPS fetch: " + e.getMessage());
        }

        if(!this.peers.isEmpty()) {
            this.logger.debug("Got host from RPS: " + this.peers.get(0).getIpAddress());
            return this.peers.remove(0);
        } else {
            throw new RandomPeerSamplingException("No peer found after query. Timed out.");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Peer queryRandomPeer(List<String> excluding) throws RandomPeerSamplingException {
        for(int i=0; i<10; i++) {
            Peer p = queryRandomPeer();
            if(!excluding.contains(p.getId()))
                return p;
        }
        this.logger.error("Failed to find a new random peer in 10 queries.");
        return null;
    }
}
