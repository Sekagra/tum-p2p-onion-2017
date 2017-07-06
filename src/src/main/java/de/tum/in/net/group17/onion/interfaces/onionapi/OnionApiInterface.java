package de.tum.in.net.group17.onion.interfaces.onionapi;

/**
 * This interface is responsible for serving incoming requests of the UI/CM thus essentially providing the Onion API
 * given in the specification of the application.
 * Created by Christoph Rudolf on 11.06.17.
 */
public interface OnionApiInterface {
    void listen();

    void sendIncoming();
}
