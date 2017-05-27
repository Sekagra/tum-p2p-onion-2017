package de.tum.in.net.group17.onion.config;

/**
 * A ConfigurationProvider is in charge of parsing and reading configurations files to supply the relevant values to
 * certain modules (e.g. ports for API connections)
 * Created by Christoph Rudolf on 27.05.17.
 */
public class ConfigurationProvider {

    public int getAuthModuleRequestPort() {
        return 9000;
    }

    public int getAuthModuleReponsePort() {
        return 9000;
    }
}
