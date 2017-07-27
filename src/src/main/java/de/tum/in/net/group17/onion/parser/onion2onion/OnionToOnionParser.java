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
    public ParsedMessage buildOnionTunnelInitMsg(byte[] incomingLidRaw, byte[] handshakePayload) throws ParsingException;

    /**
     * Create a new ONION_TUNNEL_ACCEPT message.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the yet established tunnel.
     * @param handshakePayload The handshake payload build by the Onion Auth module.
     * @return A OnionTunnelInitParsedMessage containing all parameters.
     */
    public ParsedMessage buildOnionTunnelAcceptMsg(byte[] incomingLidRaw, byte[] handshakePayload) throws ParsingException;

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
    public ParsedMessage buildOnionTunnelRelayMsg(byte[] incomingLidRaw, byte[] outgoingLidRaw,
                                                  byte[] addressRaw, short port, byte[] data) throws ParsingException;

    /**
     * Create a ONION_TUNNEL_TRANSPORT message with the given parameters.
     * This implementation throws a ParsingException on every error.
     *
     * @param incominLidRaw The LID of the incoming tunnel in the perspective of the receiving host.
     * @param innerPkt The inner packet contained in this message.
     * @return A OnionTunnelTransferParsedMessage containing the given parameters.
     */
    public ParsedMessage buildOnionTunnelTransferMsg(byte[] incominLidRaw, ParsedMessage innerPkt) throws ParsingException;

    /**
     * Build a new ONION_TUNNEL_TEARDOWN message containing the given parameters.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the incoming tunnel in the perspective of the receiving host.
     * @param timestampBlob The encrypted and integrity protected timestamp contained in the message.
     * @return A OnionTunnelTeardownParsedMessage containing the given parameters.
     */
    public ParsedMessage buildOnionTunnelTeardownMsg(byte[] incomingLidRaw, byte[] timestampBlob) throws ParsingException;


    /**
     * Build a new ONION_TUNNEL_VOICE message containing the given payload.
     *
     * @param payload The payload that shall be contained in the voice message.
     *                Length of payload and header of the resulting message must be shorter than the defined
     *                "equal size" for all VOICE messages!
     * @return A OnionTunnelVoiceParsedMessage containing the given payload.
     */
    public ParsedMessage buildOnionTunnelVoiceMsg(byte[] payload) throws ParsingException;
}
