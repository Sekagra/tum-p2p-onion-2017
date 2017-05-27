package de.tum.in.net.group17.onion.model;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * This acts as a template for callbacks given to the Authentication interface.
 * All possibly asynchronous responses of the Onion Authentication module have in common that they respond with either
 * a complete packet to be forwarded or encrypted/decrypted data. Therefore, a simply byte array serves for all methods.
 * Created by Christoph Rudolf on 27.05.17.
 */
public interface AuthResult {
    /**
     * Called once the asynchronous result has been retrieved.
     * @param result The ParsedMessage that was retrieved from the Authentication Module.
     */
    void respond(ParsedMessage result);
}
