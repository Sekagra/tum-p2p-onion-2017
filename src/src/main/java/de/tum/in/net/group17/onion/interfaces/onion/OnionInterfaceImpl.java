package de.tum.in.net.group17.onion.interfaces.onion;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.UdpClient;
import de.tum.in.net.group17.onion.interfaces.UdpServer;
import de.tum.in.net.group17.onion.interfaces.authentication.AuthenticationInterface;
import de.tum.in.net.group17.onion.model.*;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs1ParsedMessage;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionToOnionParser;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Onion to Onion interface via UDP.
 * Created by Christoph Rudolf on 21.06.17.
 */
public class OnionInterfaceImpl implements OnionInterface {
    private ConfigurationProvider config;
    private OnionToOnionParser parser;
    private int port;
    private UdpServer server;
    private UdpClient client;
    private AuthenticationInterface authInterface;

    private List<Tunnel> tunnels;
    private Map<Lid, TunnelSegment> segments;

    private Logger logger;

    @Inject
    public OnionInterfaceImpl(ConfigurationProvider config, OnionToOnionParser parser, AuthenticationInterface authInterface) {
        this.logger = Logger.getLogger(OnionInterface.class);
        this.parser = parser;
        this.config = config;
        this.port = this.config.getOnionPort();
        this.server = new UdpServer();
        this.client = new UdpClient();
        this.authInterface = authInterface;
    }

    @Override
    public void setTunnel(List<Tunnel> tunnel) {
        this.tunnels = tunnel;
    }

    @Override
    public void setSegments(Map<Lid, TunnelSegment> segments) {
        this.segments = segments;
    }

    @Override
    public void listen(OnionCallback callback)
    {
        this.logger.info("Starting to listen for incoming Onion connections on port " + this.port);
        this.server.listen(this.port, (ctx, packet) -> {
            logger.debug("Messaged received on " + new Date() + " from " + packet.sender().toString() + "\r");
            final ByteBuf bb = packet.content();
            byte[] buf = new byte[bb.readableBytes()];

            bb.readBytes(buf);

            logger.debug("Received [" + new String(buf)+ "]");
        });
    }

    @Override
    public void extendTunnel(Tunnel tunnel, Peer peer) throws OnionException {
        // Send the relay message to actually advance the tunnel
        // To do so, we have to either send a init message to the first hop for new tunnels or a relay-init message
        // wrapped in a transport packet

        // Create the relay-init message (use the currently last lid as incoming lid)
        Lid lastLid = tunnel.getSegments().get(tunnel.getSegments().size() - 1).getLid();
        Lid newLid = LidImpl.createRandomLid();
        ParsedMessage msg;
        try {
            msg = getRelayInitMessage(lastLid, newLid, peer);
        } catch (InterruptedException e) {
            throw new OnionException("Interrupted during Onion tunnel build: " + e.getMessage());
        } catch (ParsingException e) {
            throw new OnionException("Unable to parse message: " + e.getMessage());
        }

        // Wrap the relayInit message into a transport message for the first hop if there are already peers in the tunnel
        // Send the message to the new hop or the first in an existing tunnel accordingly
        if(!this.tunnels.isEmpty()) {
            try {
                TunnelSegment segment = tunnel.getSegments().get(0);
                msg = this.parser.buildOnionTunnelTransferMsg(segment.getLid().serialize(), msg);
                try {
                    this.client.send(segment.getNextAddress(), segment.getNextPort(), msg);
                } catch (IOException e) {
                    throw new OnionException("Error sending the packet to initiate the a tunnel: " + e.getMessage());
                }
            } catch (ParsingException e) {
                throw new OnionException("Error building the packet to initiate the a tunnel: " + e.getMessage());
            }
        } else {
            try {
                this.client.send(peer.getIpAddress(), peer.getPort(), msg);
            } catch (IOException e) {
                throw new OnionException("Error sending the packet to initiate the a tunnel: " + e.getMessage());
            }
        }

        // Wait for response here ?!!? How to continue

        // Advance the tunnel model by one segment if everything has been successful
        tunnel.addSegment(new TunnelSegment(newLid, peer.getIpAddress(), peer.getPort(), Direction.FORWARD));
    }

    /**
     * Creates a relay message including the init message for a given tunnel and peer.
     * This includes calling the authentication module to get the first part of the handshake and is synchronized
     * for convenience.
     * @param incomingLid The incomingLid to be put into the relay message for the extending host to create a mapping.
     * @param newLid A new local identifier to use as an identifier for the connection two the new peer.
     * @param peer The new peer to advance the tunnel with.
     */
    private ParsedMessage getRelayInitMessage(Lid incomingLid, Lid newLid, Peer peer) throws InterruptedException, ParsingException {
        // synchronize the request to the authentication module
        Object lock = new Object();

        RequestResult callback = new RequestResult() {
            @Override
            public void respond(ParsedMessage result) {
                setResult(result);
                synchronized (lock) {
                    lock.notify();
                }
            }
        };
        this.authInterface.startSession(peer, callback);

        // Wait for the async request to return
        synchronized (lock) {
            lock.wait();
        }

        ParsedMessage initMessage = this.parser.buildOnionTunnelInitMsg(incomingLid.serialize(), ((AuthSessionHs1ParsedMessage)callback.getResult()).getPayload());
        return this.parser.buildOnionTunnelRelayMsg(incomingLid.serialize(), newLid.serialize(), peer.getIpAddress().getAddress(), peer.getPort(), initMessage.serialize());
    }
}
