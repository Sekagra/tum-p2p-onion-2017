package de.tum.in.net.group17.onion.parser.rps;

/**
 * Created by Christoph Rudolf on 24.05.17.
 */
public interface RandomPeerSamplingParser {
    /**
     * Build a RPS Query message.
     * Method should throw an IllegalArgumentException on parsing errors.
     *
     * @return RpsParsedObject containing the query message.
     */
    RpsParsedObject buildRpsQueryMsg();

    /**
     * Parse an incoming message.
     * Method should throw an IllegalArgumentException on parsing errors.
     *
     * @param data Data sent by the RPS module.
     * @return RpsParseObject if a valid packet was sent by the RPS module.
     */
    RpsParsedObject parseMsg(byte[] data);
}
