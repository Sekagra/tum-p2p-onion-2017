package de.tum.in.net.group17.onion.interfaces.onion;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.UdpServer;
import de.tum.in.net.group17.onion.interfaces.authentication.AuthException;
import de.tum.in.net.group17.onion.interfaces.authentication.AuthenticationInterface;
import de.tum.in.net.group17.onion.model.*;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs1ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs2ParsedMessage;
import de.tum.in.net.group17.onion.parser.onion2onion.*;
import de.tum.in.net.group17.onion.parser.onionapi.OnionCoverParsedMessage;
import de.tum.in.net.group17.onion.parser.onionapi.OnionTunnelDataParsedMessage;
import de.tum.in.net.group17.onion.util.LidFingerprinting;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Implementation of the Onion to Onion interface via UDP.
 * Created by Christoph Rudolf on 21.06.17.
 */
public class OnionInterfaceImpl implements OnionInterface {
    private ConfigurationProvider config;
    private OnionToOnionParser parser;
    private InetAddress listenAddress;
    private int port;
    private UdpServer server;
    private AuthenticationInterface authInterface;

    /**
     * List of tunnels we have started; complete encryption + FORWARD
     */
    private Map<Integer, Tunnel> startedTunnels;

    /**
     * List of tunnels we are an endpoint to; there is only a single segment and a tunnel ID known in this case
     */
    private Map<Integer, Tunnel> incomingTunnels;

    /**
     * List of segments for every time we take the role of an intermediate hop in a tunnel; only segments, no tunnel IDs
     */
    private Map<Lid, TunnelSegment> segments;

    /**
     * Maps local identifier of previously established tunnels that have been switched to new ones in the current round.
     * We keep a mapping until the receiver answered using the newer identifier for the first time. This is done to
     * allow old in-order messages sent before getting the switching message to be handled.
     * old identifier -> old tunnel
     */
    private Map<Lid, Tunnel> toBeDestroyed;

    private Map<Lid, RequestResult> waitForAccept;
    private Logger logger;
    private OnionCallback orchestratorCallback;

    @Inject
    public OnionInterfaceImpl(ConfigurationProvider config, OnionToOnionParser parser, AuthenticationInterface authInterface) {
        this.logger = Logger.getLogger(OnionInterface.class);
        this.parser = parser;
        this.config = config;
        this.segments = new HashMap<>();
        this.toBeDestroyed = new HashMap<>();
        this.listenAddress = config.getOnionP2PHost();
        this.port = this.config.getOnionP2PPort();
        this.server = new UdpServer();
        this.authInterface = authInterface;
        this.waitForAccept = new HashMap<>();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setTunnels(Map<Integer, Tunnel> started, Map<Integer, Tunnel> incoming) {
        this.startedTunnels = started;
        this.incomingTunnels = incoming;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void listen(OnionCallback callback)
    {
        orchestratorCallback = callback;

        this.logger.info("Starting to listen for incoming Onion connections on port " + this.port);
        this.server.listen(this.listenAddress, this.port, (ctx, packet) -> {
            final ByteBuf bb = packet.content();
            byte[] buf = new byte[bb.readableBytes()];
            bb.readBytes(buf);

            logger.debug("Received on " + this.port + " with size " + buf.length + " from " + packet.sender().getPort());

            try {
                ParsedMessage parsed = parser.parseMsg(buf);
                InetSocketAddress senderSocketAddress = packet.sender();
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
    public void setSegments(Map<Lid, TunnelSegment> segments) {
        this.segments = segments;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void extendTunnel(Tunnel tunnel, Peer peer) throws OnionException, InterruptedException {
        TunnelSegment newSegment = new TunnelSegment(LidImpl.createRandomLid(), peer, Direction.FORWARD);

        // Create the init message
        ParsedMessage msg;
        try {
            AuthSessionHs1ParsedMessage hs1 = this.authInterface.startSession(peer);
            newSegment.setSessionId(hs1.getSessionId());
            msg = this.parser.buildOnionTunnelInitMsg(newSegment.getLid().serialize(), hs1.getPayload());
        } catch (InterruptedException e) {
            throw new OnionException("Interrupted during session start build: " + e.getMessage());
        } catch (ParsingException e) {
            throw new OnionException("Unable to parse message: " + e.getMessage());
        } catch (AuthException e) {
            throw new OnionException("Error from the authentication module: " + e.getMessage());
        }

        // Create a relay-init message (use the currently last lid as incoming lid)
        // Wrap the relay-init message into a transport message for the first hop if there are already peers in the tunnel
        // Send the message to the new hop or the first in an existing tunnel accordingly
        if(!tunnel.getSegments().isEmpty()) {
            try {
                Lid lastLid = tunnel.getSegments().get(tunnel.getSegments().size() - 1).getLid();

                // Wrap in relay
                msg = this.parser.buildOnionTunnelRelayMsg(lastLid.serialize(), newSegment.getLid().serialize(),
                        peer.getIpAddress().getAddress(), peer.getPort(),
                        msg.serialize());

                TunnelSegment firstSegment = tunnel.getSegments().get(0);
                // Wrap in transport
                msg = this.parser.buildOnionTunnelTransferMsgPlain(firstSegment.getLid().serialize(), msg);
                msg = this.authInterface.encrypt((OnionTunnelTransportParsedMessage)msg, tunnel.getSegments());
                try {
                    this.server.send(firstSegment.getNextAddress(), firstSegment.getNextPort(), msg.serialize());
                } catch (IOException e) {
                    throw new OnionException("Error sending the packet to initiate the a tunnel: " + e.getMessage());
                }
            } catch (ParsingException e) {
                throw new OnionException("Error building the packet to initiate the a tunnel: " + e.getMessage());
            } catch (AuthException e) {
                throw new OnionException("Error while encrypting ONION TUNNEL TRANSFER message.");
            }
        } else {
            try {
                this.server.send(peer.getIpAddress(), peer.getPort(), msg.serialize()); // send directly to new peer
            } catch (IOException e) {
                throw new OnionException("Error sending the packet to initiate the a tunnel: " + e.getMessage());
            }
        }

        // Wait for a response being there or timeout
        this.waitForAccept.put(newSegment.getLid(), new RequestResult());
        synchronized (this.waitForAccept.get(newSegment.getLid())) {
            this.waitForAccept.get(newSegment.getLid()).wait(5000);
        }

        RequestResult res = this.waitForAccept.get(newSegment.getLid());
        this.waitForAccept.remove(newSegment.getLid());
        if (res != null && res.isReturned()) {
            // Continue with accept message (if there is one)
            OnionTunnelAcceptParsedMessage acceptMsg = (OnionTunnelAcceptParsedMessage) res.getResult();
            try {
                this.authInterface.forwardIncomingHandshake2(newSegment.getSessionId(), acceptMsg.getAuthPayload());
            } catch (ParsingException e) {
                throw new OnionException("Error building the packet to forward the finalizing session handshake: " + e.getMessage());
            }

            // Advance the tunnel model by one segment if everything has been successful
            tunnel.addSegment(newSegment);
        } else {
            throw new OnionException("Error while extending the tunnel: Did not receive accept message in time!");
        }
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
                try {
                    handleTunnelAccept((OnionTunnelAcceptParsedMessage)parsedMessage, senderAddress, senderPort);
                } catch (OnionException e) {
                    logger.error("Error while sending accept message back into the tunnel: " + e.getMessage());
                }
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
                    handleDestroyedTunnel((OnionToOnionParsedMessage)parsedMessage, senderAddress, senderPort);
                } catch (InterruptedException e) {
                    logger.error("Interrupted during transport message decryption: " + e.getMessage());
                } catch (ParsingException e) {
                    logger.error("Unable to parse transport message: " + e.getMessage());
                    // todo: Specification: Teardown in case of an API violation
                } catch(IOException e) {
                    this.logger.error("Error during message forwarding, tunnel possibly went down: " + e.getMessage());
                } catch (AuthException e) {
                    this.logger.warn("Error during encrypt or decrypt of packet. Dropping the packet!");
                }
                break;
            case ONION_TUNNEL_TEARDOWN:
                try {
                    handleTunnelTeardown((OnionTunnelTeardownParsedMessage)parsedMessage);
                } catch (ParsingException e) {
                    logger.warn("Error when trying to close a session: " + e.getMessage());
                } catch (OnionException e) {
                    logger.warn("Unknown tunnel ID for tear down.");
                }
                break;
            case ONION_TUNNEL_VOICE:
                handleTunnelVoice((OnionTunnelVoiceParsedMessage)parsedMessage);
                try {
                    handleDestroyedTunnel((OnionToOnionParsedMessage)parsedMessage, senderAddress, senderPort);
                } catch (ParsingException e) {
                    this.logger.warn("This is impossible to happen, please send an error report.");
                } catch (InterruptedException e) {
                    this.logger.warn("This is impossible to happen, please send an error report.");
                }
                break;
            case ONION_TUNNEL_ESTABLISHED:
                handleTunnelEstablished((OnionTunnelEstablishedParsedMessage)parsedMessage);
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
        TunnelSegment segment = new TunnelSegment(parsedMessage.getLid(), senderAddress, senderPort, Direction.FORWARD);

        // get AuthSessionHs2ParsedMessage from auth module handshake
        try {
            AuthSessionHs2ParsedMessage response = this.authInterface.forwardIncomingHandshake1(parsedMessage.getAuthPayload());

            // build accept message
            ParsedMessage acceptMsg = this.parser.buildOnionTunnelAcceptMsg(parsedMessage.getLid().serialize(), response.getPayload());
            this.server.send(senderAddress, senderPort, acceptMsg.serialize());

            // if everything went like expected, add the state to this peer's segments list
            this.segments.put(parsedMessage.getLid(), segment);
        } catch (InterruptedException e) {
            logger.error("Interrupted during session init build: " + e.getMessage());
        } catch (ParsingException e) {
            logger.error("Unable to parse message: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Unable to send accept message: " + e.getMessage());
        } catch (AuthException e) {
            logger.error("Error from the authentication module: " + e.getMessage());
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
    private void handleTunnelAccept(OnionTunnelAcceptParsedMessage msg, InetAddress senderAddress, short senderPort) throws OnionException {
        if(this.waitForAccept.containsKey(msg.getLid())) {
            RequestResult res = this.waitForAccept.get(msg.getLid());
            res.setResult(msg);
            synchronized (res) {
                res.notify();
            }
        } else if(segments.containsKey(msg.getLid())) { // Intermediate hop + accept => Answer to relay-init
            // The relayHandler sent the init message -> Send accept through the tunnel
            TunnelSegment outgoingSegment = segments.get(msg.getLid());
            TunnelSegment incomingSegment = outgoingSegment.getOther();
            try {
                ParsedMessage relayAnswer = this.parser.buildOnionTunnelTransferMsgPlain(incomingSegment.getLid().serialize(), msg);
                relayAnswer = this.authInterface.encrypt((OnionTunnelTransportParsedMessage)relayAnswer, incomingSegment, false);
                this.server.send(incomingSegment.getNextAddress(), incomingSegment.getNextPort(), relayAnswer.serialize());
            } catch (IOException e) {
                throw new OnionException("Error sending the packet to initiate the a tunnel: " + e.getMessage());
            } catch (ParsingException e) {
                throw new OnionException("Error parsing the ONION TUNNEL TRANSFER message containing the relay answer :" +
                        e.getMessage());
            } catch (InterruptedException e) {
                throw new OnionException("Interrupted while encrypting accept message: " + e.getMessage());
            } catch (AuthException e) {
                throw new OnionException("Error while encrypting ONION TUNNEL TRANSFER message");
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
            outgoingSegment.setOther(incomingSegment);

            this.segments.put(outgoingSegment.getLid(), outgoingSegment);

            // send the expected encapsulated message out to the new node and adapt the peer's own state
            this.server.send(msg.getAddress(), msg.getPort(), msg.getPayload());
        } else {
            this.logger.warn("Received incoming relay message for extending a tunnel with an unknown local identifier. Dropping the packet now.");
            return;
        }
    }

    /***
     * Handle an incoming transport message that has to be decrypted at least once.
     *
     * @param msg The incoming parsed OnionTunnelTransportParsedMessage message.
     * @param senderAddress InetAddress of the sender of this datagram.
     * @param senderPort The remote port of the sender of this datagram.
     */
    private void handleTunnelTransport(OnionTunnelTransportParsedMessage msg, InetAddress senderAddress, short senderPort)
            throws IOException, ParsingException, InterruptedException, AuthException {
        Lid lid = msg.getLid();

        // check Lid in TunnelSegment list (case: intermediate hop or receiver)
        TunnelSegment segment = this.segments.get(lid);
        if(segment != null) {
            if(segment.getDirection() == Direction.FORWARD) {
                msg = this.authInterface.decrypt(msg, segment);
                if(msg.forMe()) {   // if direction is forward, decrypt and check magic bytes
                    this.handleReceiving(this.parser.parseMsg(msg.getInnerPacket()), senderAddress, senderPort);   // reinvoke handling for inner packet
                } else {
                    // if not for us (magic bytes not matching) replace Lid and forward to successor
                    msg.setLid(segment.getOther().getLid());
                    this.server.send(segment.getOther().getNextAddress(), segment.getOther().getNextPort(), msg.serialize());
                    segment.updateLastDataSeen();
                }
            } else if (segment.getDirection() == Direction.BACKWARD) {
                // if direction is BACKWARD, encrypt once and hand to predecessor
                msg = this.authInterface.encrypt(msg, segment.getOther(), true);
                if(segment.getOther() != null) {
                    msg.setLid(segment.getOther().getLid());
                    this.server.send(segment.getOther().getNextAddress(), segment.getOther().getNextPort(), msg.serialize());
                    segment.updateLastDataSeen();
                } else {
                    this.logger.error("Unable to forward transport message backwards through the tunnel due to missing segment.");
                }
            }
        } else {    // initiator gets message
            Optional<Tunnel> tunnel = this.startedTunnels.values().stream()
                    .filter(t -> !t.getSegments().isEmpty() && t.getSegments().get(0).getLid().equals(lid))
                    .findAny();
            if(tunnel.isPresent()) {
                // decrypt the complete onion as this message is for us
                this.handleReceiving(this.parser.parseMsg(this.authInterface.decrypt(msg, tunnel.get().getSegments()).getInnerPacket()), senderAddress, senderPort);
                tunnel.get().getSegments().get(0).updateLastDataSeen();
            } else {
                logger.warn("Received ONION TUNNEL TRANSPORT message for unknown tunnel!");
            }
        }
    }

    /***
     * Handle an incoming teardown message.
     *
     * @param msg The incoming parsed OnionTunnelTeardownParsedMessage message.
     */
    private void handleTunnelTeardown(OnionTunnelTeardownParsedMessage msg) throws ParsingException, OnionException {
        // If we receive a tunnel teardown for a tunnel we are an intermediate hop for, tear it down
        TunnelSegment segment = this.segments.get(msg.getLid());
        if(segment != null && segment.getDirection() == Direction.FORWARD) {
            this.segments.remove(msg.getLid());
            if(segment.getOther() != null) {
                this.segments.remove(segment.getOther());
            }
            this.authInterface.closeSession(segment.getSessionId());

            // Possible necessity to remove incomingTunnel state
            this.incomingTunnels.values().removeIf(t -> !t.getSegments().isEmpty() && t.getSegments().get(0).getLid().equals(msg.getLid()));

            // Might be a teardown for a previously switched out tunnel that can now be removed
            this.toBeDestroyed.remove(msg.getLid());
            return;
        }

        // If we receive a tunnel teardown as the tunnel originator here, the tunnel endpoint has issued it
        Optional<Tunnel> tunnel = this.startedTunnels.values().stream()
                .filter(t -> !t.getSegments().isEmpty() && t.getSegments().get(t.getSegments().size() - 1).getLid().equals(msg.getLid()))
                .findAny();
        if(tunnel.isPresent()) {
                this.destroyTunnelById(tunnel.get().getId());
        } else {
            this.logger.warn("Received unsolicited teardown with unknown local identifier: " + msg);
        }
    }

    /***
     * Handle an incoming voice message with plain voice data.
     *
     * @param msg The incoming parsed OnionTunnelVoiceParsedMessage message.
     */
    private void handleTunnelVoice(OnionTunnelVoiceParsedMessage msg) {
        // Determine the tunnel ID matching to this message
        Optional<Tunnel> tunnel = this.startedTunnels.values().stream()
                .filter(t -> !t.getSegments().isEmpty() && t.getSegments().get(t.getSegments().size() - 1).getLid().equals(msg.getLid()))
                .findAny();
        Optional<Map.Entry<Integer, Tunnel>> entry = this.incomingTunnels.entrySet().stream()
                .filter(xs -> !xs.getValue().getSegments().isEmpty() && xs.getValue().getSegments().get(0).getLid().equals(msg.getLid()))
                .findAny();

        if(tunnel.isPresent()) {
            this.orchestratorCallback.tunnelData(tunnel.get().getId(), msg.getData());
        } else if(entry.isPresent()) {
            this.orchestratorCallback.tunnelData(entry.get().getKey(), msg.getData());
        } else {
            logger.warn("Received voice message with unknown/ambiguous local identifier. Dropping it.");
        }
    }

    /**
     * Handle an incoming ONION_TUNNEL_ESTABLISHED message (either contact the superordinate module or conduct a
     * refresh of a tunnel).
     *
     * @param msg The established type message to handle.
     */
    private void handleTunnelEstablished(OnionTunnelEstablishedParsedMessage msg) {
        TunnelSegment segment = segments.get(msg.getLid());
        if(!msg.isRefresh()) {    // normal established
            this.orchestratorCallback.tunnelIncoming(segment);
            return;
        } else {    // refresh a tunnel state transparently
            Optional<Map.Entry<Integer, Tunnel>> entry = this.incomingTunnels.entrySet().stream()
                    .filter(t -> !t.getValue().getSegments().isEmpty() && t.getValue().getSegments().get(0).getLid().equals(msg.getLidOld()))
                    .findAny();

            if(entry.isPresent()) {
                // save old state and remap
                this.toBeDestroyed.put(msg.getLidOld(), entry.get().getValue());
                // identical tunnel ID, new tunnel
                Tunnel tunnel = new Tunnel(entry.get().getKey());
                tunnel.addSegment(segment);
                this.incomingTunnels.put(entry.get().getKey(), tunnel);
            } else {
                this.logger.warn("Being asked to refresh an unknown tunnel, nice try NSA ;)");
            }
        }
    }

    /**
     * This method handles possible leftovers from switched out tunnels
     * @param msg The received message for which we now check if there is possible a tunnel we switched out accociated
     *            with its local identifier.
     * @param senderAddress InetAddress of the sender of this datagram.
     * @param senderPort The remote port of the sender of this datagram.
     */
    private void handleDestroyedTunnel(OnionToOnionParsedMessage msg, InetAddress senderAddress, short senderPort) throws ParsingException, InterruptedException {
        // check type
        if(msg.getType() == MessageType.ONION_TUNNEL_TRANSPORT) {
            // check if this LID is part of toBeDestroyed, decrypt and forward it accordingly
            Tunnel tunnel = this.toBeDestroyed.get(msg.getLid());
            if(tunnel != null) {
                try {
                    this.handleReceiving(this.parser.parseMsg(this.authInterface.decrypt((OnionTunnelTransportParsedMessage) msg, tunnel.getSegments()).getInnerPacket()), senderAddress, senderPort);
                } catch (AuthException e) {
                    this.logger.warn("Error during decrypt of packet received on switched out tunnel. Dropping packet!");
                }
            } else { // Received data on a regular tunnel
                // check if this LID has an old LID associated with it in toBeDestroyed (associated via Tunnel ID)

                // get tunnel ID of incoming Lid
                Optional<Integer> tunnelId = this.startedTunnels.values().stream()
                        .filter(t -> !t.getSegments().isEmpty() && t.getSegments().get(0).getLid().equals(msg.getLid()))
                        .map(t -> t.getId()).findAny();

                if(tunnelId.isPresent()) {  // check for associated destroyed entry
                    Optional<Map.Entry<Lid, Tunnel>> entry = this.toBeDestroyed.entrySet().stream()
                            .filter(e -> e.getValue().getId() == tunnelId.get())
                            .findAny();
                    if(entry.isPresent()) {
                        // Note: The first data with a new lid removes the intermediate mapping and issues a teardown on the old tunnel
                        try {
                            this.destroyTunnel(entry.get().getValue());
                            this.toBeDestroyed.remove(entry.get().getKey());
                        } catch (OnionException e) {
                            this.logger.warn("Could not destroy switched out tunnel: " + e.getMessage());
                        }
                    }
                }
            }
        } else if(msg.getType() == MessageType.ONION_TUNNEL_VOICE) {
            // get the tunnel ID of the LID and transmit this voice packet
            Tunnel tunnel = this.toBeDestroyed.get(msg.getLid());
            if(tunnel != null) {
                this.orchestratorCallback.tunnelData(tunnel.getId(), ((OnionTunnelVoiceParsedMessage)msg).getData());
            }
        } else {
            this.logger.warn("This is impossible to happen, please send an error report.");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void destroyTunnelById(int tunnelId) throws OnionException {
        // Determine if this is a tunnel we created or not, handle differently
        Tunnel tunnel = this.startedTunnels.get(tunnelId);

        if(tunnel != null) {
            destroyTunnel(tunnel);
        } else { // we are supposed to be a receiver for this tunnel here
            tunnel = this.incomingTunnels.get(tunnelId);
            if(tunnel != null) {
                destroyTunnel(tunnel);
            } else {
                throw new OnionException("Requested to teardown unknown tunnel with ID: " + tunnelId);
            }
        }

        this.orchestratorCallback.tunnelDestroyed(tunnelId);
    }


    /**
     * Destroy a concrete tunnel given by a Tunnel-instance.
     * @param tunnel The tunnel instance to destroy.
     */
    private void destroyTunnel(Tunnel tunnel) throws OnionException {
        if(tunnel.getSegments().isEmpty()) {
            throw new OnionException("Cannot destroy empty tunnel!");
        }
        TunnelSegment firstSegment = tunnel.getSegments().get(0);
        List<ParsedMessage> transportPackets = new ArrayList<>();

        try {
            for(int i=tunnel.getSegments().size() - 1; i >= 0; i--){
                TunnelSegment segment = tunnel.getSegments().get(i);
                ParsedMessage teardownPacket = this.parser.buildOnionTunnelTeardownMsg(segment.getLid().serialize());
                ParsedMessage transportPacket = this.parser.buildOnionTunnelTransferMsgPlain(firstSegment.getLid().serialize(), teardownPacket);
                transportPackets.add(this.authInterface.encrypt((OnionTunnelTransportParsedMessage)transportPacket, tunnel.getSegments()));

                // close the session associated with the segment
                authInterface.closeSession(segment.getSessionId());

                // remove the segment
                tunnel.getSegments().remove(i);
            }

            // send all teardown messages
            for(ParsedMessage transportPacket : transportPackets) {
                this.server.send(firstSegment.getNextAddress(), firstSegment.getNextPort(), transportPacket.serialize());
                Thread.sleep(333);
            }
        } catch (ParsingException e) {
            this.logger.error("Unable to build required teardown or transport data packet to send out a teardown message: " + e.getMessage());
        } catch (InterruptedException e) {
            this.logger.error("Unable to encrypt a message via the authentication module: " + e.getMessage());
        } catch (IOException e) {
            this.logger.error("Unable to send message to next peer: " + e.getMessage());
        } catch (AuthException e) {
            throw new OnionException("Error during encryption of ONION TUNNEL TEARDOWN message: " + e.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendVoiceData(OnionTunnelDataParsedMessage msg) throws OnionException {
        this.sendVoiceData(msg.getTunnelId(), msg.getData());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendCoverData(OnionCoverParsedMessage msg) throws OnionException {
        // create random cover data
        byte[] coverData = new byte[msg.getCoverSize()];
        new Random().nextBytes(coverData);

        // there can only be one tunnel if we send cover traffic
        if(this.startedTunnels.size() > 0) {
            this.sendVoiceData(this.startedTunnels.get(0).getId(), coverData);
        } else if(this.incomingTunnels.size() > 0) {
            this.sendVoiceData(this.incomingTunnels.keySet().iterator().next(), coverData);
        } else {
            this.logger.error("No tunnel to send cover traffic through!");
        }
    }

    private void sendVoiceData(int tunnelId, byte[] data) throws OnionException {
        // expect a matching tunnel ID in either the list of created or incoming tunnels
        Tunnel tunnel = this.startedTunnels.get(tunnelId);
        TunnelSegment firstSegment;
        TunnelSegment lastSegment;

        logger.debug("Using tunnel " + tunnelId + " to send a voice message!");
        try {
            if(tunnel != null) {
                // tunnel started by us
                firstSegment = tunnel.getSegments().get(0);
                lastSegment = tunnel.getSegments().get(tunnel.getSegments().size() - 1);
            } else if(this.incomingTunnels.containsKey(tunnelId) && !this.incomingTunnels.get(tunnelId).getSegments().isEmpty()) {
                // tunnel we are an endpoint to
                firstSegment = lastSegment = this.incomingTunnels.get(tunnelId).getSegments().get(0);
            } else {
                this.logger.error("Unable to send data on unknown tunnel with ID: " + tunnelId);
                return;
            }

            // create the voice messages and handle each one
            List<ParsedMessage> voicePackets = this.parser.buildOnionTunnelVoiceMsgs(lastSegment.getLid().serialize(), data);
            for (ParsedMessage voicePacket : voicePackets) {
                ParsedMessage transportPacket = this.parser.buildOnionTunnelTransferMsgPlain(firstSegment.getLid().serialize(), voicePacket);
                // encrypt accordingly
                if(tunnel != null) {
                    transportPacket = this.authInterface.encrypt((OnionTunnelTransportParsedMessage)transportPacket, tunnel.getSegments());
                } else {
                    transportPacket = this.authInterface.encrypt((OnionTunnelTransportParsedMessage)transportPacket, firstSegment, false);
                }
                this.server.send(firstSegment.getNextAddress(), firstSegment.getNextPort(), transportPacket.serialize());
                firstSegment.updateLastDataSeen();
            }
        } catch (ParsingException e) {
            this.logger.error("Unable to build required voice or transport data packet to send out a voice message: " + e.getMessage());
        } catch (InterruptedException e) {
            this.logger.error("Unable to encrypt a message via the authentication module: " + e.getMessage());
        } catch (IOException e) {
            this.logger.error("Unable to send message to next peer: " + e.getMessage());
        } catch (AuthException e) {
            throw new OnionException("Cannot encrypt ONION TUNNEL TRANSPORT package: " + e.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendEstablished(Tunnel tunnel) throws OnionException {
        if(!tunnel.getSegments().isEmpty()) {
            try {
                ParsedMessage msg = this.parser.buildOnionTunnelEstablishedMsg(tunnel.getSegments().get(tunnel.getSegments().size() - 1).getLid().serialize());
                ParsedMessage transportPacket = this.parser.buildOnionTunnelTransferMsgPlain(tunnel.getSegments().get(0).getLid().serialize(), msg);
                transportPacket = this.authInterface.encrypt((OnionTunnelTransportParsedMessage)transportPacket, tunnel.getSegments());
                this.server.send(tunnel.getSegments().get(0).getNextAddress(), tunnel.getSegments().get(0).getNextPort(), transportPacket.serialize());
            } catch (ParsingException e) {
                throw new OnionException("Unable to build established message or transport data packet to send over tunnel: " + e.getMessage());
            } catch (InterruptedException e) {
                throw new OnionException("Unable to encrypt a message via the authentication module: " + e.getMessage());
            } catch (IOException e) {
                throw new OnionException("Unable to send established message to next peer: " + e.getMessage());
            } catch (AuthException e) {
                throw new OnionException("Cannot encrypt established message: " + e.getMessage());
            }
        } else {
            this.logger.error("Cannot send established on empty tunnel.");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void sendEstablished(Tunnel newTunnel, Tunnel oldTunnel) throws OnionException {
        if(!oldTunnel.getSegments().isEmpty() && !newTunnel.getSegments().isEmpty()) {
            try {
                TunnelSegment firstNewTunnelSegment = newTunnel.getSegments().get(0);
                TunnelSegment lastNewTunnelSegment = newTunnel.getSegments().get(newTunnel.getSegments().size() - 1);
                TunnelSegment lastOldTunnelSegment = oldTunnel.getSegments().get(oldTunnel.getSegments().size() - 1);
                ParsedMessage msg = this.parser.buildOnionTunnelEstablishedMsg(lastNewTunnelSegment.getLid().serialize(), lastOldTunnelSegment.getLid().serialize());
                ParsedMessage transportPacket = this.parser.buildOnionTunnelTransferMsgPlain(firstNewTunnelSegment.getLid().serialize(), msg);
                transportPacket = this.authInterface.encrypt((OnionTunnelTransportParsedMessage)transportPacket, newTunnel.getSegments());
                this.server.send(firstNewTunnelSegment.getNextAddress(), firstNewTunnelSegment.getNextPort(), transportPacket.serialize());

                // Create mapping to be able to handle old incoming data until the receiver switched to the new keys
                this.toBeDestroyed.put(oldTunnel.getSegments().get(0).getLid(), oldTunnel);

            } catch (ParsingException e) {
                throw new OnionException("Unable to build established message or transport data packet to send over tunnel: " + e.getMessage());
            } catch (InterruptedException e) {
                throw new OnionException("Unable to encrypt a message via the authentication module: " + e.getMessage());
            } catch (IOException e) {
                throw new OnionException("Unable to send established message to next peer: " + e.getMessage());
            } catch (AuthException e) {
                throw new OnionException("Cannot encrypt established message during tunnel switching: " + e.getMessage());
            }
        } else {
            this.logger.error("Cannot send established for empty tunnels.");
        }
    }
}
