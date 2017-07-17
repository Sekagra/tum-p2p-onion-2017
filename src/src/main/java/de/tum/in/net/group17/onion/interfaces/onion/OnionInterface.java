package de.tum.in.net.group17.onion.interfaces.onion;

/**
 * This interface is responsible for serving incoming requests of fellow Onion modules and sending requests to them.
 * It is responsible for all Onion to Onion communication.
 * Created by Christoph Rudolf on 21.06.17.
 */
public interface OnionInterface {
    void listen();
}
