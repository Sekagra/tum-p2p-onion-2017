package de.tum.in.net.group17.onion.interfaces.rps;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParser;
import org.apache.log4j.Logger;


/**
 * Implementation of the RandomPeerSamplingInterface using Netty.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class RandomPeerSamplingInterfaceImpl extends TcpClientInterface implements RandomPeerSamplingInterface {
    private RandomPeerSamplingParser parser;
    private ConfigurationProvider config;
    private Logger logger;

    /**
     * Create a new RPS interface.
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received from the RPS module.
     */
    @Inject
    public RandomPeerSamplingInterfaceImpl(ConfigurationProvider config, RandomPeerSamplingParser parser) {
        super(config.getAuthModuleHost(), config.getAuthModuleRequestPort());
        this.logger = Logger.getLogger(RandomPeerSamplingInterface.class);
        this.parser = parser;
        this.config = config;
    }

    public void queryRandomPeer(final RequestResult callback) {
        // Build the query message
        ParsedMessage packet = this.parser.buildRpsQueryMsg();

        sendMessage(packet.serialize(), result -> callback.respond(parser.parseMsg(result)));
    }
}
