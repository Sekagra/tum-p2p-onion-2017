package de.tum.in.net.group17.onion.interfaces;

import de.tum.in.net.group17.onion.parser.ParsedMessage;

/**
 * This acts as a template for callbacks given to any server interface.
 * All possibly responses of other modules have in common that they respond with either
 * a complete packet to be forwarded or encrypted/decrypted data. Therefore, a simply byte array serves for all methods.
 * Created by Christoph Rudolf on 27.05.17.
 */
public interface RawRequestResult {
    /**
     * Called once the asynchronous result has been retrieved.
     * @param result The unparsed message that was retrieved from the other module.
     */
    void respond(byte[] result);
}
