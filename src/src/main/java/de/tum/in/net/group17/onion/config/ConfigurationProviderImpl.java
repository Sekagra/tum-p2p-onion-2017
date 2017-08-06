package de.tum.in.net.group17.onion.config;

import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.NoSuchFileException;
import java.util.regex.Pattern;

/**
 * A ConfigurationProviderImpl is in charge of parsing and reading configurations files to supply the relevant values to
 * certain modules (e.g. ports for API connections)
 * Created by Christoph Rudolf on 27.05.17.
 */
public class ConfigurationProviderImpl implements ConfigurationProvider {
    private Logger logger;

    /*
     * We load all required values on startup and print an error if some parameter is missing.
     * Therefore, our module does not crash during operation because a configuration parameter is missing.
     */
    private int onionP2PPort, onionApiPort, authApiPort, rpsApiPort;
    private InetAddress onionP2PHost, onionApiHost, authApiHost, rpsApiHost;
    private int intermediateHopCount;

    public ConfigurationProviderImpl(String configPath) throws NoSuchFileException, InvalidFileFormatException {
        this.logger = Logger.getLogger(ConfigurationProvider.class);

        File configFile = new File(configPath);
        if(!configFile.exists() || configFile.isDirectory()) {
            logger.fatal("Configuration file does not exist: " + configPath);
            throw new NoSuchFileException("Configuration file does not exist: " + configPath);
        }

        Wini configuration = null;
        try {
            configuration = new Wini(configFile);
        } catch (IOException e) {
            throw new InvalidFileFormatException("Could not parse configuration file: " + configPath + "; " +
                    e.getMessage());
        }

        try {
            intermediateHopCount = configuration.get("onion", "intermediate_hops", int.class);

            String addrPort = configuration.get("onion", "listen_address");
            try {
                onionP2PHost = getAddressFromString(addrPort);
                onionP2PPort = getPortFromString(addrPort);
            } catch(InvalidFileFormatException e) {
                throw new InvalidFileFormatException("Could not parse onion/listen_address: " + e.getMessage());
            }

            addrPort = configuration.get("onion", "api_address");
            try {
                onionApiHost = getAddressFromString(addrPort);
                onionApiPort = getPortFromString(addrPort);
            } catch(InvalidFileFormatException e) {
                throw new InvalidFileFormatException("Could not parse onion/api_address: " + e.getMessage());
            }

            addrPort = configuration.get("rps", "api_address");
            try {
                rpsApiHost = getAddressFromString(addrPort);
                rpsApiPort = getPortFromString(addrPort);
            } catch(InvalidFileFormatException e) {
                throw new InvalidFileFormatException("Could not parse rps/api_address: " + e.getMessage());
            }

            addrPort = configuration.get("auth", "api_address");
            try {
                authApiHost = getAddressFromString(addrPort);
                authApiPort = getPortFromString(addrPort);
            } catch(InvalidFileFormatException e) {
                throw new InvalidFileFormatException("Could not parse auth/api_address: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new InvalidFileFormatException("Could not access required value from configuration file: " +
                    e.getMessage());
        }
    }

    @Override
    public int getOnionApiPort() {
        return onionApiPort;
    }

    @Override
    public int getOnionP2PPort() {
        return onionP2PPort;
    }

    @Override
    public int getAuthApiPort() {
        return authApiPort;
    }

    @Override
    public InetAddress getOnionP2PHost() {
        return onionP2PHost;
    }

    @Override
    public InetAddress getOnionApiHost() {
        return onionApiHost;
    }

    @Override
    public InetAddress getAuthApiHost() {
        return authApiHost;
    }

    @Override
    public InetAddress getRpsApiHost() {
        return rpsApiHost;
    }

    @Override
    public int getRpsApiPort() {
        return rpsApiPort;
    }

    @Override
    public int getIntermediateHopCount() {
        return intermediateHopCount;
    }

    /**
     * Get the address contained in a string of format <ipv4:port> or <[ipv6]:port>.
     *
     * @param addressPort The string containing the port.
     * @return The address given in the input string.
     * @throws InvalidFileFormatException On any parsing error.
     */
    private InetAddress getAddressFromString(String addressPort) throws InvalidFileFormatException {
        if(addressPort.contains("[")) {
            try {
                String address = Pattern.compile("(\\[[0-9:]*\\]).*").matcher(addressPort).group(0);
                return InetAddress.getByName(address);
            } catch(UnknownHostException | IllegalStateException | IndexOutOfBoundsException e) {
                throw new InvalidFileFormatException("Could not parse IPv6 address: " + addressPort);
            }
        } else {
            try {
                return InetAddress.getByName(addressPort.split(":")[0]);
            } catch (UnknownHostException e) {
                throw new InvalidFileFormatException("Could not parse IPv4 address: " + addressPort);
            }
        }
    }

    /**
     * Get the port contained in a string of format <ipv4:port> or <[ipv6]:port>.
     *
     * @param addressPort The string containing the port.
     * @return The port given in the input string.
     * @throws InvalidFileFormatException On any parsing error.
     */
    private int getPortFromString(String addressPort) throws InvalidFileFormatException {
        if(addressPort.contains("[")) {
            try {
                return new Integer(Pattern.compile("\\[.*\\]:([0-9])*").matcher(addressPort).group(0));
            } catch(IllegalStateException | IndexOutOfBoundsException e) {
                throw new InvalidFileFormatException("Could not parse port after IPv6 address: " + addressPort);
            }
        } else {
            try {
                return new Integer(addressPort.split(":")[1]);
            } catch (IndexOutOfBoundsException e) {
                throw new InvalidFileFormatException("Could not parse after IPv4 address: " + addressPort);
            }
        }
    }
}
