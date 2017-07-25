package de.tum.in.net.group17.onion.interfaces.onionapi;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpClientInterface;
import de.tum.in.net.group17.onion.interfaces.TcpServerInterface;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.onionapi.*;
import org.apache.log4j.Logger;

import java.net.InetAddress;

/**
 * Implementation of the Onion API interface offering services to the UI/CM.
 * Created by Christoph Rudolf on 11.06.17.
 */
public class OnionApiInterfaceImpl implements OnionApiInterface {
    private OnionApiParser parser;
    private ConfigurationProvider config;
    private OnionApiCallback callback;
    private Logger logger;

    private TcpClientInterface client;
    private TcpServerInterface server;

    /**
     * Create a new Onion API interface.
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received at the Onion API interface.
     */
    @Inject
    public OnionApiInterfaceImpl(ConfigurationProvider config, OnionApiParser parser) {
        this.logger = Logger.getLogger(OnionApiInterface.class);
        this.parser = parser;
        this.config = config;

        this.client = new TcpClientInterface(InetAddress.getLoopbackAddress(), this.config.getOnionApiPort());
        this.server = new TcpServerInterface() {
            @Override
            protected void readIncoming(byte[] msg) {
                readIncoming(msg);
            }
        };
    }

    public void listen(OnionApiCallback callback) {
        this.callback = callback;
        this.server.listen(this.config.getOnionApiPort());
    }

    /**
     * Read a plain unparsed message from the TCP channel.
     * @param msg The data to be read via TCP.
     */
    protected void readIncoming(byte[] msg) {
        ParsedMessage parsedMsg = null;
        try {
            parsedMsg = parser.parseMsg(msg);
            switch(parsedMsg.getType()) {
                case ONION_TUNNEL_BUILD:
                    this.callback.receivedTunnelBuild((OnionTunnelBuildParsedMessage) parsedMsg);
                case ONION_TUNNEL_DESTROY:
                    this.callback.receivedDestroy((OnionTunnelDestroyParsedMessage) parsedMsg);
                case ONION_TUNNEL_DATA:
                    this.callback.receivedVoiceData((OnionTunnelDataParsedMessage) parsedMsg);
                case ONION_COVER:
                    this.callback.receivedCoverData((OnionCoverParsedMessage) parsedMsg);
            }
        } catch (ParsingException e) {
            logger.error("Received invalid packet: " + e.getMessage());
        }
    }

    public void sendIncoming() {

    }

    public void sendReady() {

    }

    public void sendError() {

    }
}
