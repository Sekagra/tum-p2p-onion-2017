package de.tum.in.net.group17.onion.parser.rps;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;
import org.bouncycastle.asn1.ASN1InputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.05.17.
 *
 * Parser for incoming and outgoing RPS messges.
 * The class covers the first version of the Voidphone RPS API.
 */
public class RandomPeerSamplingParserImpl extends VoidphoneParser implements RandomPeerSamplingParser {

    /**
     * Build a RPS Query message.
     * The method throws and ParsingException on every parse error!
     *
     * @return The RpsParsedObject containing the RPS Query message.
     */
    public ParsedMessage buildRpsQueryMsg() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putShort((short)4);
        buffer.putShort((short)MessageType.RPS_QUERY.getValue());

        return createParsedMessage(buffer.array());
    }

    /**
     * Parse an incoming RPS message.
     * The method throws and ParsingException on every parse error!
     *
     * @param data Incoming packet.
     * @return RpsParsedObject containing the message type and packet data if parsing succeeds.
     */
    public ParsedMessage parseMsg(byte[] data) {
        checkSize(data); // Throws an exception if an error occurs
        ByteBuffer buffer = ByteBuffer.wrap(data);

        switch(extractType(data)) {
            case RPS_PEER:
                return parseRpsPeerMsg(data);
            default:
                throw new ParsingException("Not able to parse message. Type: " + extractType(data).getValue() + "!");
        }
    }

    /**
     * Parse an RPS PEER message.
     * The method throws an ParsingException on every parsing error!
     *
     * @param data Array containing the packet to parse.
     * @return RpsParsedObject of type RPS_MSG_TYPE.RPS_PEER if the packet is a valid RPS PEER message.
     */
    private ParsedMessage parseRpsPeerMsg(byte[] data) {
        if (data.length < 13) // Contains header, port, res, IP address (IPv4 here!) and key (No length known)
            throw new ParsingException("Packet is too short to contain a header, an IP and a hostkey!");

        ByteBuffer buffer = ByteBuffer.wrap(data);
        // TODO: We assume to have IPv4 addresses -> Change to handle both v4 and v6 if we know how to do this

        // We do not have to check validity of the given IP address: Only some random bits for us.

        byte[] key = new byte[data.length - 12];
        buffer.position(12);
        buffer.get(key);

        try {
            new ASN1InputStream(new ByteArrayInputStream(key)).readObject();
        } catch (IOException e) {
           throw new ParsingException("Invalid hostkey!");
        }

        return createParsedMessage(data);
    }
}
