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
import de.tum.in.net.group17.onion.parser.onion2onion.OnionTunnelAcceptParsedMessage;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
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

    private Map<Lid, OnionTunnelAcceptParsedMessage> waitForAccept;

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
        this.waitForAccept = new HashMap<>();
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
            final ByteBuf bb = packet.content();
            byte[] buf = new byte[bb.readableBytes()];
            bb.readBytes(buf);

            // match lid with waitForAccept and TunnelSegments
            try {
                ParsedMessage parsed = parser.parseMsg(buf);
                switch(parsed.getType()) {
                    case ONION_TUNNEL_INIT:
                        break;
                    case ONION_TUNNEL_ACCEPT:
                        OnionTunnelAcceptParsedMessage acceptMsg = (OnionTunnelAcceptParsedMessage)parsed;
                        if(this.waitForAccept.containsKey(acceptMsg.getLid())) {
                            this.waitForAccept.put(acceptMsg.getLid(), acceptMsg);
                            synchronized (this.waitForAccept) {
                                this.waitForAccept.notify();
                            }
                        } else {
                            logger.info("Received unsolicited or late accept message for an extended tunnel.");
                        }
                        break;
                    case ONION_TUNNEL_TRANSPORT:    // all other ONION types would be encapsulated in this
                        //todo: decrypt with all sessions in matching tunnel
                        break;
                }

            } catch (ParsingException e) {
                logger.warn("Received invalid message over Onion P2P interface.");
            }

        });
    }

    @Override
    public void extendTunnel(Tunnel tunnel, Peer peer) throws OnionException, InterruptedException {
        TunnelSegment segment = new TunnelSegment(LidImpl.createRandomLid(), peer.getIpAddress(), peer.getPort(), Direction.FORWARD);
        Lid lastLid = tunnel.getSegments().get(tunnel.getSegments().size() - 1).getLid();

        // Create the relay-init message (use the currently last lid as incoming lid)
        ParsedMessage msg;
        try {
            AuthSessionHs1ParsedMessage hs1 = startSessionSynchronous(peer);
            segment.setSessionId(hs1.getSessionId());
            msg = this.parser.buildOnionTunnelRelayMsg(lastLid.serialize(), segment.getLid().serialize(),
                    peer.getIpAddress().getAddress(), peer.getPort(),
                    this.parser.buildOnionTunnelInitMsg(lastLid.serialize(), hs1.getPayload()).serialize());
        } catch (InterruptedException e) {
            throw new OnionException("Interrupted during session start build: " + e.getMessage());
        } catch (ParsingException e) {
            throw new OnionException("Unable to parse message: " + e.getMessage());
        }

        // Wrap the relayInit message into a transport message for the first hop if there are already peers in the tunnel
        // Send the message to the new hop or the first in an existing tunnel accordingly
        if(!tunnel.getSegments().isEmpty()) {
            try {
                TunnelSegment firstSegment = tunnel.getSegments().get(0);
                msg = this.parser.buildOnionTunnelTransferMsg(firstSegment.getLid().serialize(), msg);
                try {
                    this.client.send(firstSegment.getNextAddress(), firstSegment.getNextPort(), msg);
                } catch (IOException e) {
                    throw new OnionException("Error sending the packet to initiate the a tunnel: " + e.getMessage());
                }
            } catch (ParsingException e) {
                throw new OnionException("Error building the packet to initiate the a tunnel: " + e.getMessage());
            }
        } else {
            try {
                this.client.send(peer.getIpAddress(), peer.getPort(), msg); // send directly to new peer
            } catch (IOException e) {
                throw new OnionException("Error sending the packet to initiate the a tunnel: " + e.getMessage());
            }
        }

        // Wait for a response being there or timeout
        this.waitForAccept.put(segment.getLid(), null);
        synchronized (this.waitForAccept) {
            this.waitForAccept.wait(5000);
        }

        // Continue with accept message (if there is one)
        OnionTunnelAcceptParsedMessage acceptMsg = this.waitForAccept.get(segment.getLid());
        if(acceptMsg == null) {
            // This has to be a timeout, remove pending accept
            this.waitForAccept.remove(segment.getLid());
            throw new OnionException("Timeout when waiting for tunnel accept message.");
            // Todo: check if notifyAll is needed which would break this test here
        }
        try {
            this.authInterface.forwardIncomingHandshake2(peer, segment.getSessionId(), acceptMsg.getAuthPayload());
        } catch (ParsingException e) {
            throw new OnionException("Error building the packet to forward the finalizing session handshake: " + e.getMessage());
        }

        // Advance the tunnel model by one segment if everything has been successful
        tunnel.addSegment(segment);
    }

    /**
     * Starts a new session synchronously and returns the AuthSessionHs1 data.
     * @param peer The new peer to advance the tunnel with.
     *
     * @return The first handshake messages to forward to the new peer.
     */
    private AuthSessionHs1ParsedMessage startSessionSynchronous(Peer peer) throws InterruptedException, ParsingException {
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
            lock.wait(5000);
        }

        if(callback.getResult() == null)
            throw new InterruptedException("Timeout.");

        return (AuthSessionHs1ParsedMessage)callback.getResult();
    }
}
