package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;

/**
 * Created by Marko Dorfhuber(PraMiD) on 28.05.17.
 *
 * Provides methods to create and parse Onion2Onion API messages.
 */
public interface OnionToOnionParser {
    /**
     * Set the given relay message into 'encrypted' state by providing the encrypted data.
     * This message throws a ParsingException on every error.
     *
     * @param msg The ONTION_TUNNEL_RELAY message we encrypted.
     * @param data The BLOB containing the encrypted information.
     */
    public void setEncryptedRelayData(OnionTunnelRelayParsedMessage msg, byte[] data);

    /**
     * Set the given ONION_TUNNEL_RELAY message in the 'unencrypted' state.
     * We set the information contained in the data parameter which shall be decrypted data blob contained in the
     * encrypted ONION_TUNNEL_RELAY message.
     *
     * @param msg The Onion message we want to set to 'unencrypted' state.
     * @param data The unencrypted data.
     */
    public void setUnencryptedRelayData(OnionTunnelRelayParsedMessage msg, byte[] data);

    /**
     * Create a new ONION_TUNNEL_INIT message.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the new tunnel.
     * @param handshakePayload The handshake payload build by the Onion Auth module.
     * @return A OnionTunnelInitParsedMessage containing all parameters.
     */
    public ParsedMessage buildOnionTunnelInitMsg(byte[] incomingLidRaw, byte[] handshakePayload);

    /**
     * Create a new ONION_TUNNEL_ACCEPT message.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the yet established tunnel.
     * @param handshakePayload The handshake payload build by the Onion Auth module.
     * @return A OnionTunnelInitParsedMessage containing all parameters.
     */
    public ParsedMessage buildOnionTunnelAcceptMsg(byte[] incomingLidRaw, byte[] handshakePayload);

    /**
     * Create a new unencrypted ONION_TUNNE_RELAY message.
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
                                                  byte[] addressRaw, short port, byte[] data);

    /**
     * Create a ONION_TUNNEL_TRANSPORT message with the given parameters.
     * This implementation throws a ParsingException on every error.
     *
     * @param incominLidRaw The LID of the incoming tunnel in the perspective of the receiving host.
     * @param data The data contained in the message.
     * @return A OnionTunnelTransferParsedMessage containing the given parameters.
     */
    public ParsedMessage buildOnionTunnelTransferMsg(byte[] incominLidRaw, byte[] data);

    /**
     * Build a new ONION_TUNNEL_TEARDOWN message containing the given parameters.
     * This implementation throws a ParsingException on every error.
     *
     * @param incomingLidRaw The LID of the incoming tunnel in the perspective of the receiving host.
     * @param timestampBlob The encrypted and integrity protected timestamp contained in the message.
     * @return A OnionTunnelTeardownParsedMessage containing the given parameters.
     */
    public ParsedMessage buildOnionTunnelTeardownMsg(byte[] incomingLidRaw, byte[] timestampBlob);

    /**
     * @inheritDoc
     *
     * This implementation throws a ParsingException on every error.
     */
    public ParsedMessage parseMsg(byte[] data);
}
