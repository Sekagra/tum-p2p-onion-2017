package de.tum.in.net.group17.onion.parser.rps;

import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.Parser;
import de.tum.in.net.group17.onion.parser.ParsingException;

/**
 * Created by Christoph Rudolf on 24.05.17.
 * Implementation by Marko Dorfhuber(PraMiD) on 25.05.17.
 *
 * This interface provides methods to created outgoing messages to the RPS module and the check validity of incoming
 * messages.
 */
public interface RandomPeerSamplingParser extends Parser {
    /**
     * Build a RPS Query message.
     *
     * @return The RpsParsedObject containing the RPS Query message.
     */
    ParsedMessage buildRpsQueryMsg() throws ParsingException;
}
