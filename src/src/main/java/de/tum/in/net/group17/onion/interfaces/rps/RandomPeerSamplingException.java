package de.tum.in.net.group17.onion.interfaces.rps;

/**
 * Exception for all errors happening while using the RPS module.
 * Created by Christoph Rudolf on 25.07.17.
 */
public class RandomPeerSamplingException extends Exception {
    public RandomPeerSamplingException(String msg) {
        super(msg);
    }
}
