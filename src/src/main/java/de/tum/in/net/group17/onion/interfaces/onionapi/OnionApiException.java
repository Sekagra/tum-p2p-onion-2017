package de.tum.in.net.group17.onion.interfaces.onionapi;

/**
 * Created by Marko Dorfhuber(PraMiD) on 30.07.17.
 *
 * The OnionApi will throw an OnionApiException on any error.
 */
public class OnionApiException extends Exception {
    /**
     * Create a new OnionApiException with the given message.
     *
     * @param msg A message describing the error.
     */
    public OnionApiException(String msg)
    {
        super(msg);
    }
}
