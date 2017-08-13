package de.tum.in.net.group17.onion.interfaces.onionapi;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.TcpServerInterface;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.onionapi.*;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Onion API interface offering services to the UI/CM.
 * Created by Christoph Rudolf on 11.06.17.
 */
public class OnionApiInterfaceImpl extends TcpServerInterface implements OnionApiInterface {
    private OnionApiParser parser;
    private ConfigurationProvider config;
    private OnionApiCallback callback;
    private Logger logger;

    private Map<Integer, Channel> clients; // Map tunnelIDs to corresponding clients
    private Channel lastClient;

    /**
     * Create a new Onion API interface.
     * @param config The configuration of the Onion module to read listening ports and other values from.
     * @param parser The parser for packets that are expected to be received at the Onion API interface.
     */
    @Inject
    public OnionApiInterfaceImpl(ConfigurationProvider config, OnionApiParser parser) {
        super();

        this.logger = LogManager.getLogger(OnionApiInterface.class);
        this.parser = parser;
        this.config = config;
        this.clients = Collections.synchronizedMap(new HashMap<Integer, Channel>());
    }

    public void listen(OnionApiCallback callback) {
        this.callback = callback;
        this.listen(this.config.getOnionApiHost(), this.config.getOnionApiPort());
    }

    /**
     * Read a plain unparsed message from the TCP channel.
     * @param msg The data to be read via TCP.
     */
    protected void readIncoming(byte[] msg, Channel channel) {
        ParsedMessage parsedMsg = null;
        try {
            parsedMsg = parser.parseMsg(msg);
            switch(parsedMsg.getType()) {
                case ONION_TUNNEL_BUILD:
                    try {
                        checkChannelState(channel);
                        this.callback.receivedTunnelBuild((OnionTunnelBuildParsedMessage) parsedMsg);
                        // Remember the client -> If we send a TUNNEL READY message we will create the mapping
                        lastClient = channel;
                    } catch(OnionApiException e) {
                        logger.error("Received message on invalid channel: " + e.getMessage());
                    }
                    break;
                case ONION_TUNNEL_DESTROY:
                    this.callback.receivedDestroy((OnionTunnelDestroyParsedMessage) parsedMsg);
                    // Remove the mapping for this tunnelID
                    this.clients.remove(((OnionTunnelDestroyParsedMessage) parsedMsg).getTunnelId());
                    break;
                case ONION_TUNNEL_DATA:
                    this.callback.receivedVoiceData((OnionTunnelDataParsedMessage) parsedMsg);
                    break;
                case ONION_COVER:
                    this.callback.receivedCoverData((OnionCoverParsedMessage) parsedMsg);
                    break;
                default:
                    logger.error("Received invalid message on OnionAPI.");
            }
        } catch (ParsingException e) {
            channel.close(); // This is an API violation -> Close the channel
            logger.error("Received invalid packet: " + e.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendIncoming(int tunnelId) throws OnionApiException {
        try {
            checkChannelState(lastClient); // We inform the last client that connected to the onion module
            clients.put(tunnelId, lastClient); // Add a mapping for the new tunnel
            lastClient.writeAndFlush(parser.buildOnionTunnelIncomingMsg(tunnelId).serialize());
        } catch(OnionApiException e) {
            throw new OnionApiException("No valid last client: " + e.getMessage());
        } catch(ParsingException e) {
            throw new OnionApiException("Could not create the ONION TUNNEL INCOMING message: " + e.getMessage());
        }
    }


    /**
     * @inheritDoc
     */
    public void sendReady(int tunnelId, byte[] key) throws OnionApiException
    {
        try {
            checkChannelState(lastClient);

            // A tunnel is ready -> add the mapping for the last Client that sent us a build message
            clients.put(tunnelId, lastClient);

            ParsedMessage msg = parser.buildOnionTunnelReadyMsg(tunnelId, key);
            lastClient.writeAndFlush(msg.serialize());
        } catch (OnionApiException e) {
            if(lastClient != null)
                lastClient.close();
            lastClient = null;
            throw new OnionApiException("Not able to send ONION TUNNEL READY message: " + e.getMessage());
        } catch (ParsingException e) {
            throw new OnionApiException("Could not parse ONION TUNNEL READY message: " + e.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendError(int tunnelId, MessageType type) throws OnionApiException {
        try {
            ParsedMessage packet = this.parser.buildOnionErrorMsg(type, tunnelId);
        } catch (ParsingException e) {
            throw new OnionApiException("Unable to build ONION TUNNEL ERROR message: " + e.getMessage());
        }

        /**
         *
         * Tunnel ID might not be set because we specify the tunnel ID and if an error happens during build, whom to
         * send this back to?!
         * What about lastClient?
         *
         */
        Channel chn = clients.get(tunnelId);
        if(chn == null) {
            try {
                checkChannelState(lastClient);
                chn = lastClient;
            } catch(OnionApiException e) {
                if (lastClient != null) {
                    if (chn != null)
                        chn.close();
                    lastClient = null;
                }
                throw new OnionApiException("Could not send ONION ERROR message. " +
                        "No mapping for the given tunnel ID and no 'lastClient' is stored: " + e.getMessage());
            }
        }

        try {
            // Check if it is possible to send data through the channel.
            checkChannelState(chn);

            ParsedMessage msg = parser.buildOnionErrorMsg(type, tunnelId);
            chn.writeAndFlush(chn.writeAndFlush(msg.serialize()));
        } catch (OnionApiException e) {
            clients.remove(tunnelId);
            if(chn != null)
                chn.close();
            throw new OnionApiException("Could not send ONION ERROR message. " +
                    "The channel that corresponds to tunnel ID " + tunnelId + " is in an invalid state!" +
                    e.getMessage());
        } catch (ParsingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendVoiceData(int tunnelId, byte[] msg) throws OnionApiException
    {
        Channel chn = clients.get(tunnelId);
        if(chn == null)
            throw new OnionApiException("No mapping for this tunnel ID!");
        try {
            // Check if it is possible to send data through the channel.
            checkChannelState(chn);
            chn.writeAndFlush(this.parser.buildOnionDataMsg(tunnelId, msg).serialize());
        } catch (OnionApiException e) {
            clients.remove(tunnelId);
            chn.close();
            throw new OnionApiException("Cannot send ONION TUNNEL DATA message." +
                    "The channel that corresponds to tunnel ID " + tunnelId + " is in an invalid state!" +
                    e.getMessage());
        } catch (ParsingException e) {
            throw new OnionApiException("Could not build ONION TUNNEL DATA message: " + e.getMessage());
        }
    }

    /**
     * Check the state of this channel.
     *
     * @param chn The channel to check.
     * @throws OnionApiException If the channel has an invalid state. The message indicates the reason for the
     * invalid state.
     */
    private void checkChannelState(Channel chn) throws OnionApiException
    {
        if(chn == null)
            throw new OnionApiException("Invalid channel!");
        if(!chn.isOpen() || !chn.isWritable())
            throw new OnionApiException("Channel has an invalid state!");
    }
}
