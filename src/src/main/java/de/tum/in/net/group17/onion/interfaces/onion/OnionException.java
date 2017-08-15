package de.tum.in.net.group17.onion.interfaces.onion;

/**
 * Exception for all errors happening while using the Onion P2P interface.
 * Created by Christoph Rudolf on 29.07.17.
 */
public class OnionException extends Exception {
    /**
     * Create a new OnionException with the given message.
     *
     * @param msg A message describing the error.
     */
    public OnionException(String msg) {
        super(msg);
    }
}