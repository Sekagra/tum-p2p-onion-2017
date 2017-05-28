package de.tum.in.net.group17.onion.parser.rps;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * Created by Christoph Rudolf on 24.05.17.
 * Implementation by Marko Dorfhuber(PraMiD) on 25.05.17.
 *
 * This interface provides methods to created outgoing messages to the RPS module and the check validity of incoming
 * messages.
 */
public interface RandomPeerSamplingParser {
    /**
     * Build a RPS Query message.
     * Method should throw an IllegalArgumentException on parsing errors.
     *
     * @return ParsedMessage containing the query message.
     */
    ParsedMessage buildRpsQueryMsg();

    /**
     * Parse an incoming message.
     * Method should throw an IllegalArgumentException on parsing errors.
     *
     * @param data Data sent by the RPS module.
     * @return ParsedMessage if a valid packet was sent by the RPS module.
     */
    ParsedMessage parseMsg(byte[] data);
}
