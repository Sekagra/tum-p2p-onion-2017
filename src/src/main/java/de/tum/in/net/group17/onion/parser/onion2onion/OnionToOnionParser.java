package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.Parser;
import de.tum.in.net.group17.onion.parser.ParsingException;

/**
 * Created by Marko Dorfhuber(PraMiD) on 28.05.17.
 *
 * Provides methods to create and parse Onion2Onion API messages.
 */
public interface OnionToOnionParser extends Parser {
    /**
     * Create a new ONION_TUNNEL_INIT message.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the new tunnel.
     * @param handshakePayload The handshake payload build by the Onion Auth module.
     * @return A OnionTunnelInitParsedMessage containing all parameters.
     */
    ParsedMessage buildOnionTunnelInitMsg(byte[] incomingLidRaw, byte[] handshakePayload) throws ParsingException;

    /**
     * Create a new ONION_TUNNEL_ACCEPT message.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the yet established tunnel.
     * @param handshakePayload The handshake payload build by the Onion Auth module.
     * @return A OnionTunnelInitParsedMessage containing all parameters.
     */
    ParsedMessage buildOnionTunnelAcceptMsg(byte[] incomingLidRaw, byte[] handshakePayload) throws ParsingException;

    /**
     * Create a new unencrypted ONION_TUNNEL_RELAY message.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the incoming tunnel in the perspective of the relaying host.
     * @param outgoingLidRaw The new LID in the perspective of the relaying host.
     * @param addressRaw The address of new host.
     * @param port The port the new host listens on.
     * @param data The data the relay host shall send to the new host
     * @return A OnionTunnelRelayParsedMessage containing all parameters.
     */
    ParsedMessage buildOnionTunnelRelayMsg(byte[] incomingLidRaw, byte[] outgoingLidRaw,
                                                  byte[] addressRaw, short port, byte[] data) throws ParsingException;

    /**
     * Create a ONION_TUNNEL_TRANSPORT message with unencrypted inner data. Further encryption is supposed to happen
     * on the completed OnionTunnelTransferParsedMessage.
     *
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the incoming tunnel in the perspective of the receiving host.
     * @param innerPkt The inner packet contained in this message.
     * @return A OnionTunnelTransportParsedMessage containing the given parameters.
     */
    ParsedMessage buildOnionTunnelTransferMsgPlain(byte[] incomingLidRaw, ParsedMessage innerPkt) throws ParsingException;

    /**
     * Build a new ONION_TUNNEL_TEARDOWN message containing the given parameters.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the incoming tunnel in the perspective of the receiving host.
     * @return A OnionTunnelTeardownParsedMessage containing the given parameters.
     */
    ParsedMessage buildOnionTunnelTeardownMsg(byte[] incomingLidRaw) throws ParsingException;


    /**
     * Build a new ONION_TUNNEL_VOICE message containing the given payload.
     *
     * @param payload The payload that shall be contained in the voice message.
     *                Length of payload and header of the resulting message must be shorter than the defined
     *                "equal size" for all VOICE messages!
     * @return A OnionTunnelVoiceParsedMessage containing the given payload.
     */
    ParsedMessage buildOnionTunnelVoiceMsg(byte[] payload) throws ParsingException;
}
