package de.tum.in.net.group17.onion.model.results;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * This acts as a template for callbacks given to any server interface.
 * This response type already includes the parsing of the message that is being retrieved
 * Created by Christoph Rudolf on 06.06.17.
 */
public class RequestResult {
    private ParsedMessage result;

    /**
     * Indicator if a response was returned to a given request.
     *
     * @return True if we received a response.
     */
    public boolean isReturned() {
        return this.result != null;
    }

    /**
     * Set the received response.
     *
     * @param result The received response.
     */
    public void setResult(ParsedMessage result) {
        this.result = result;
    }

    /**
     * Get the received response.
     *
     * @return The received response.
     */
    public ParsedMessage getResult() {
        return result;
    }
}
