package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * Created by Marko Dorfhuber(PraMiD) on 26.07.17.
 *
 * This class is the base class for all ONION AUTH messages and is used to have generic access to the requestID.
 * Objects of this class may only be created by an AuthenticationParser.
 */
public abstract class AuthParsedMessage extends ParsedMessage
{
    protected final int requestId;

    /**
     * Create a new AuthParsedMessage with a given request ID.
     * Such objects may only be created by an AuthenticationParser.
     *
     * @param requestId The requestID contained in this message.
     */
    public AuthParsedMessage(int requestId) {
        this.requestId = requestId;
    }

    /**
     * Get the request ID of this message.
     *
     * @return The request ID.
     */
    public int getRequestId() {
        return requestId;
    }
}
