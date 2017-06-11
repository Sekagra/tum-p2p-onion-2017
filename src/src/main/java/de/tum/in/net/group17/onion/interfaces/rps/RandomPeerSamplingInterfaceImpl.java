package de.tum.in.net.group17.onion.interfaces.rps;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.model.results.RawRequestResult;
import de.tum.in.net.group17.onion.interfaces.RequestInterfaceBase;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParser;


/**
 * Implementation of the RandomPeerSamplingInterface using Netty.
 * Created by Christoph Rudolf on 06.06.17.
 */
public class RandomPeerSamplingInterfaceImpl extends RequestInterfaceBase implements RandomPeerSamplingInterface {
    private RandomPeerSamplingParser parser;
    private ConfigurationProvider config;

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
