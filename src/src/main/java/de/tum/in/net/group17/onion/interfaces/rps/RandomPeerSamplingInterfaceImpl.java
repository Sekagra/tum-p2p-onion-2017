package de.tum.in.net.group17.onion.interfaces.rps;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.model.results.RawRequestResult;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterfaceBase;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParser;


/**
 * Implementation of the RandomPeerSamplingInterface using Netty.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class RandomPeerSamplingInterfaceImpl extends TcpClientInterfaceBase implements RandomPeerSamplingInterface {
    private RandomPeerSamplingParser parser;
    private ConfigurationProvider config;

    /**
     * Create a new RPS interface.
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received from the RPS module.
     */
    public RandomPeerSamplingInterfaceImpl(ConfigurationProvider config, RandomPeerSamplingParser parser) {
        this.parser = parser;
        this.config = config;
        this.host = this.config.getAuthModuleHost();
        this.port = this.config.getAuthModuleRequestPort();
    }

    public void queryRandomPeer(final RequestResult callback) {
        // Build the query message
        ParsedMessage packet = this.parser.buildRpsQueryMsg();

        sendMessage(packet.serialize(), new RawRequestResult() {
            public void respond(byte[] result) {
                callback.respond(parser.parseMsg(result));
            }
        });
    }
}
