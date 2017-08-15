package de.tum.in.net.group17.onion.parser;

/**
 * Basic interfaces shared by all parsers.
 * Created by Christoph Rudolf on 25.07.17.
 */
public interface Parser {
    /**
     * Parse an incoming message.
     *
     *
     * @param data Incoming packet.
     *
     * @return ParsedMessage containing the message type and packet data if parsing succeeds.
     *
     * @throws ParsingException If the incoming message is invalid.
     */
    ParsedMessage parseMsg(byte[] data) throws ParsingException;
}
