package de.tum.in.net.group17.onion.model.results;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * This acts as a template for callbacks given to any server interface.
 * This response type already includes the parsing of the message that is being retrieved
 * Created by Christoph Rudolf on 06.06.17.
 */
public interface RequestResult {
    /**
     * Called once the asynchronous result has been retrieved.
     * @param result The parsed message that was retrieved from the other module.
     */
    void respond(ParsedMessage result);
}
