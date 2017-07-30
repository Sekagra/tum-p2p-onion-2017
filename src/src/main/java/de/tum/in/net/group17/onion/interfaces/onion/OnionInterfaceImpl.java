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
import de.tum.in.net.group17.onion.parser.onion2onion.*;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;

import java.io.IOException;
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

            try {
                ParsedMessage parsed = parser.parseMsg(buf);
                handleReceiving(parsed);
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
                msg = this.parser.buildOnionTunnelTransferMsg(firstSegment.getLid().serialize(), msg.serialize());
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
     *
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

    /**
     * General handling for all incoming packets. Designed to be reinvoked after decryption of inner packets.
     *
     * @param parsedMessage The already parsed message that confirms to a ONION P2P type
     */
    private void handleReceiving(ParsedMessage parsedMessage) {
        switch(parsedMessage.getType()) {
            case ONION_TUNNEL_INIT:
                handleTunnelInit((OnionTunnelInitParsedMessage)parsedMessage);
                break;
            case ONION_TUNNEL_ACCEPT:
                handleTunnelAccept((OnionTunnelAcceptParsedMessage)parsedMessage);
                break;
            case ONION_TUNNEL_RELAY:
                handleTunnelRelay((OnionTunnelRelayParsedMessage)parsedMessage);
                break;
            case ONION_TUNNEL_TRANSPORT:
                handleTunnelTransport((OnionTunnelTransportParsedMessage)parsedMessage);
                break;
            case ONION_TUNNEL_TEARDOWN:
                handleTunnelTeardown((OnionTunnelTeardownParsedMessage)parsedMessage);
                break;
            case ONION_TUNNEL_VOICE:
                handleTunnelVoice((OnionTunnelVoiceParsedMessage)parsedMessage);
                break;
            default:
                logger.warn("Unexpected message type, received type: " + parsedMessage.getType().toString());
        }
    }

    /**
     * Handle an incoming tunnel init message. Respond with the correct handshake and establish a new tunnel segment.
     *
     * @param parsedMessage The incoming parsed OnionTunnelInitParsedMessage message.
     */
    private void handleTunnelInit(OnionTunnelInitParsedMessage parsedMessage) {
        //todo: implement for intermediate hop functionality
        //(establish state and respond with answer of the auth module)
    }

    /**
     * Handle an incoming tunnel accept message. Find out if there has been a connection attempt waiting for this
     * response and handle the response accordingly.
     *
     * @param msg The incoming parsed OnionTunnelAcceptParsedMessage message.
     */
    private void handleTunnelAccept(OnionTunnelAcceptParsedMessage msg) {
        if(this.waitForAccept.containsKey(msg.getLid())) {
            this.waitForAccept.put(msg.getLid(), msg);
            synchronized (this.waitForAccept) {
                this.waitForAccept.notify();
            }
        } else {
            logger.info("Received unsolicited or late ACCEPT-message for an extended tunnel.");
        }
    }

    /**
     * Handle an incoming tunnel relay message by expanding the tunnel with the expected inner init-message and
     * update the own state.
     *
     * @param msg The incoming parsed OnionTunnelRelayParsedMessage message.
     */
    private void handleTunnelRelay(OnionTunnelRelayParsedMessage msg) {
        //todo: implement for intermediate hop functionality
    }

    /***
     * Handle an incoming transport message that has to be decrypted at least once.
     *
     * @param msg The incoming parsed OnionTunnelTransportParsedMessage message.
     */
    private void handleTunnelTransport(OnionTunnelTransportParsedMessage msg) {
        //todo: check Lid and direction
        //todo: if direction is BACKWARD, encrypt once and hand to predecessor
        //todo: if direction is FORWARD, decrypt, check magic bytes
        //todo:    if not for us (magic bytes not matching) forward to sucessor
        //todo:    if it is for us extract inner packet and reinvoke handleReceiving, possible an issue with unavailable parsers, e.g. relay?!
    }

    /***
     * Handle an incoming teardown message.
     *
     * @param msg The incoming parsed OnionTunnelTeardownParsedMessage message.
     */
    private void handleTunnelTeardown(OnionTunnelTeardownParsedMessage msg) {

    }

    /***
     * Handle an incoming voice message with plain voice data.
     *
     * @param msg The incoming parsed OnionTunnelVoiceParsedMessage message.
     */
    private void handleTunnelVoice(OnionTunnelVoiceParsedMessage msg) {

    }
}
