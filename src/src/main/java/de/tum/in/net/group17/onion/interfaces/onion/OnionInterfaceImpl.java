package de.tum.in.net.group17.onion.interfaces.onion;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.UdpClient;
import de.tum.in.net.group17.onion.interfaces.UdpServer;
import de.tum.in.net.group17.onion.interfaces.authentication.AuthenticationInterface;
import de.tum.in.net.group17.onion.model.*;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs1ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs2ParsedMessage;
import de.tum.in.net.group17.onion.parser.onion2onion.*;
import de.tum.in.net.group17.onion.parser.onionapi.OnionCoverParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private OnionCallback orchestratorCallback;

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

    /**
     * @inheritDoc
     */
    @Override
    public void setTunnel(List<Tunnel> tunnel) {
        this.tunnels = tunnel;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setSegments(Map<Lid, TunnelSegment> segments) {
        this.segments = segments;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void listen(OnionCallback callback)
    {
        orchestratorCallback = callback;

        this.logger.info("Starting to listen for incoming Onion connections on port " + this.port);
        this.server.listen(this.port, (ctx, packet) -> {
            final ByteBuf bb = packet.content();
            byte[] buf = new byte[bb.readableBytes()];
            bb.readBytes(buf);

            try {
                ParsedMessage parsed = parser.parseMsg(buf);
                InetSocketAddress senderSocketAddress = ((InetSocketAddress)ctx.channel().remoteAddress());
                handleReceiving(parsed, senderSocketAddress.getAddress(), (short)senderSocketAddress.getPort());
            } catch (ParsingException e) {
                logger.warn("Received invalid message over Onion P2P interface.");
            }
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void extendTunnel(Tunnel tunnel, Peer peer) throws OnionException, InterruptedException {
        TunnelSegment segment = new TunnelSegment(LidImpl.createRandomLid(), peer.getIpAddress(), peer.getPort(), Direction.FORWARD);
        Lid lastLid = tunnel.getSegments().get(tunnel.getSegments().size() - 1).getLid();

        // Create the relay-init message (use the currently last lid as incoming lid)
        ParsedMessage msg;
        try {
            AuthSessionHs1ParsedMessage hs1 = this.authInterface.startSession(peer);
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
                msg = this.parser.buildOnionTunnelTransferMsgPlain(firstSegment.getLid().serialize(), msg);
                msg = this.authInterface.encrypt((OnionTunnelTransportParsedMessage)msg, tunnel);
                try {
                    this.client.send(firstSegment.getNextAddress(), firstSegment.getNextPort(), msg.serialize());
                } catch (IOException e) {
                    throw new OnionException("Error sending the packet to initiate the a tunnel: " + e.getMessage());
                }
            } catch (ParsingException e) {
                throw new OnionException("Error building the packet to initiate the a tunnel: " + e.getMessage());
            }
        } else {
            try {
                this.client.send(peer.getIpAddress(), peer.getPort(), msg.serialize()); // send directly to new peer
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
            this.authInterface.forwardIncomingHandshake2(segment.getSessionId(), acceptMsg.getAuthPayload());
        } catch (ParsingException e) {
            throw new OnionException("Error building the packet to forward the finalizing session handshake: " + e.getMessage());
        }

        // Advance the tunnel model by one segment if everything has been successful
        tunnel.addSegment(segment);
    }

    /**
     * General handling for all incoming packets. Designed to be reinvoked after decryption of inner packets.
     *
     * @param parsedMessage The already parsed message that confirms to a ONION P2P type
     * @param senderAddress InetAddress of the sender of this datagram.
     * @param senderPort The remote port of the sender of this datagram.
     */
    private void handleReceiving(ParsedMessage parsedMessage, InetAddress senderAddress, short senderPort) {
        switch(parsedMessage.getType()) {
            case ONION_TUNNEL_INIT:
                handleTunnelInit((OnionTunnelInitParsedMessage)parsedMessage, senderAddress, senderPort);
                break;
            case ONION_TUNNEL_ACCEPT:
                handleTunnelAccept((OnionTunnelAcceptParsedMessage)parsedMessage, senderAddress, senderPort);
                break;
            case ONION_TUNNEL_RELAY:
                try {
                    handleTunnelRelay((OnionTunnelRelayParsedMessage)parsedMessage);
                } catch (IOException e) {
                    this.logger.error("Unable to extend tunnel building towards the targeted peer." + e.getMessage());
                }
                break;
            case ONION_TUNNEL_TRANSPORT:
                try {
                    handleTunnelTransport((OnionTunnelTransportParsedMessage)parsedMessage, senderAddress, senderPort);
                } catch (InterruptedException e) {
                    logger.error("Interrupted during transport message decryption: " + e.getMessage());
                } catch (ParsingException e) {
                    logger.error("Unable to parse transport message: " + e.getMessage());
                } catch(IOException e) {
                    this.logger.error("Error during message forwarding, tunnel possibly went down: " + e.getMessage());
                }
                //todo: tear down tunnel in case of an exception (that's why they are caught here)
                break;
            case ONION_TUNNEL_TEARDOWN:
                handleTunnelTeardown((OnionTunnelTeardownParsedMessage)parsedMessage);
                break;
            case ONION_TUNNEL_VOICE:
                handleTunnelVoice((OnionTunnelVoiceParsedMessage)parsedMessage);
                break;
            default:
                logger.error("Unexpected message type, received type: " + parsedMessage.getType().toString());
        }
    }

    /**
     * Handle an incoming tunnel init message. Respond with the correct handshake and establish a new tunnel segment.
     *
     * @param parsedMessage The incoming parsed OnionTunnelInitParsedMessage message.
     * @param senderAddress InetAddress of the sender of this datagram.
     * @param senderPort The remote port of the sender of this datagram.
     */
    private void handleTunnelInit(OnionTunnelInitParsedMessage parsedMessage, InetAddress senderAddress, short senderPort) {
        TunnelSegment segment = new TunnelSegment(parsedMessage.getLid(), senderAddress, senderPort, Direction.BACKWARD);

        // get AuthSessionHs2ParsedMessage from auth module handshake
        try {
            AuthSessionHs2ParsedMessage response = this.authInterface.forwardIncomingHandshake1(parsedMessage.getAuthPayload());

            // build accept message
            ParsedMessage acceptMsg = this.parser.buildOnionTunnelAcceptMsg(parsedMessage.getLid().serialize(), response.getPayload());
            this.client.send(senderAddress, senderPort, acceptMsg.serialize());

            // if everything went like expected, add the state to this peer's segments list
            this.segments.put(parsedMessage.getLid(), segment);
        } catch (InterruptedException e) {
            logger.error("Interrupted during session init build: " + e.getMessage());
        } catch (ParsingException e) {
            logger.error("Unable to parse message: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Unable to send accept message: " + e.getMessage());
        }
    }

    /**
     * Handle an incoming tunnel accept message. Find out if there has been a connection attempt waiting for this
     * response and handle the response accordingly.
     *
     * @param msg The incoming parsed OnionTunnelAcceptParsedMessage message.
     * @param senderAddress InetAddress of the sender of this datagram.
     * @param senderPort The remote port of the sender of this datagram.
     */
    private void handleTunnelAccept(OnionTunnelAcceptParsedMessage msg, InetAddress senderAddress, short senderPort) {
        if(this.waitForAccept.containsKey(msg.getLid())) {
            this.waitForAccept.put(msg.getLid(), msg);
            synchronized (this.waitForAccept) {
                this.waitForAccept.notify();
            }
        } else {
            logger.warn("Received unsolicited or late ACCEPT-message from " + senderAddress.getHostAddress() + ":" + Short.toString(senderPort));
        }
    }

    /**
     * Handle an incoming tunnel relay message by expanding the tunnel with the expected inner init-message and
     * update the own state.
     *
     * @param msg The incoming parsed OnionTunnelRelayParsedMessage message.
     */
    private void handleTunnelRelay(OnionTunnelRelayParsedMessage msg) throws IOException {
        // adapt peer's own state first (just so we don't run into extremely quick responses not being able to get handled)
        TunnelSegment incomingSegment = this.segments.get(msg.getLid());
        if(incomingSegment != null) {
            TunnelSegment outgoingSegment = new TunnelSegment(msg.getOutgoingTunnel(), msg.getAddress(), msg.getPort(), Direction.BACKWARD);
            incomingSegment.setOther(outgoingSegment);
        } else {
            this.logger.warn("Received incoming relay message for extending a tunnel with an unknown local identifier. Dropping the packet now.");
        }

        // send the expected encapsulated message out to the new node and adapt the peer's own state
        this.client.send(msg.getAddress(), msg.getPort(), msg.getPayload());
    }

    /***
     * Handle an incoming transport message that has to be decrypted at least once.
     *
     * @param msg The incoming parsed OnionTunnelTransportParsedMessage message.
     * @param senderAddress InetAddress of the sender of this datagram.
     * @param senderPort The remote port of the sender of this datagram.
     */
    private void handleTunnelTransport(OnionTunnelTransportParsedMessage msg, InetAddress senderAddress, short senderPort)
            throws IOException, ParsingException, InterruptedException {
        Lid lid = msg.getLid();

        // check Lid in TunnelSegment list (case: intermediate hop)
        TunnelSegment segment = this.segments.get(lid);
        if(segment != null) {
            if(segment.getDirection() == Direction.FORWARD) {
                msg = this.authInterface.decrypt(msg, segment);
                if(msg.forMe()) {   // if direction is forward, decrypt and check magic bytes
                    this.handleReceiving(this.parser.parseMsg(msg.getInnerPacket()), senderAddress, senderPort);   // reinvoke handling for inner packet
                } else {
                    // if not for us (magic bytes not matching) replace Lid and forward to successor
                    msg.setLid(segment.getLid());
                    this.client.send(segment.getNextAddress(), segment.getNextPort(), msg.serialize());
                }
            } else if (segment.getDirection() == Direction.BACKWARD) {
                // if direction is BACKWARD, encrypt once and hand to predecessor
                msg = this.authInterface.encrypt(msg, segment);
                if(segment.getOther() != null) {
                    msg.setLid(segment.getLid());
                    this.client.send(segment.getOther().getNextAddress(), segment.getOther().getNextPort(), msg.serialize());
                } else {
                    this.logger.error("Unable to forward transport message backwards through the tunnel due to missing segment.");
                }
            }
        } else {
            List<Tunnel> matches = this.tunnels.stream().filter(t -> !t.getSegments().isEmpty() && t.getSegments().get(0).getLid() == lid).collect(Collectors.toList());
            if(matches.size() == 1) {
                // decrypt the complete onion as this message is for us
                this.handleReceiving(this.parser.parseMsg(this.authInterface.decrypt(msg, matches.get(0)).getInnerPacket()), senderAddress, senderPort);
            } else {
                logger.warn("Received transport message with unknown/ambiguous local identifier. Dropping it.");
            }
        }
    }

    /***
     * Handle an incoming teardown message.
     *
     * @param msg The incoming parsed OnionTunnelTeardownParsedMessage message.
     */
    private void handleTunnelTeardown(OnionTunnelTeardownParsedMessage msg) {
        // Remove the matching TunnelSegment from this peer's list and sent a matching teardown message back to the
        // previous peer.
        if(this.segments.containsKey(msg.getLid())) {
            this.segments.remove(msg.getLid());
        } else {
            this.logger.warn("Received teardown instruction for unknown local identifier.");
        }
    }

    /***
     * Handle an incoming voice message with plain voice data.
     *
     * @param msg The incoming parsed OnionTunnelVoiceParsedMessage message.
     */
    private void handleTunnelVoice(OnionTunnelVoiceParsedMessage msg) {
        // Determine the tunnel ID matching to this message
        List<Tunnel> matches = this.tunnels.stream().filter(t -> !t.getSegments().isEmpty() && t.getSegments().get(0).getLid() == msg.getLid()).collect(Collectors.toList());
        if(matches.size() == 1) {
            this.orchestratorCallback.tunnelData(matches.get(0).getId(), msg.getData());
        } else {
            logger.warn("Received voice message with unknown/ambiguous local identifier. Dropping it.");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void destroyTunnel(int tunnelId) {
        //todo: destroy a tunnel we own
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendVoiceData(OnionTunnelDataParsedMessage msg) {
        //todo: encrypt and send voice data for whole tunnel
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendCoverData(OnionCoverParsedMessage msg) {
        //todo: generate encrypt and send cover data for whole tunnel
    }
}
