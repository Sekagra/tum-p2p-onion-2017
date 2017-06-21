package de.tum.in.net.group17.onion.parser.rps;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
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
     * @inheritDoc
     *
     * The method throws and ParsingException on every parse error!
     */
    public ParsedMessage buildRpsQueryMsg() {
        return new RpsQueryParsedMessage();
    }

    /**
     * @inheritDoc
     *
     * The method throws and ParsingException on every parse error!
     */
    public ParsedMessage parseMsg(byte[] data) {
        checkSize(data); // Throws an exception if an error occurs

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
        InetAddress ipAddress;
        ASN1Primitive key;
        ByteBuffer buffer;
        byte[] addr;

        if (data.length < 13) // Contains header, port, res, IP address (IPv4 here!) and key (No length known)
            throw new ParsingException("Packet is too short to contain a header, an IP and a hostkey!");
        checkType(data, MessageType.RPS_PEER); // Will throw a parsing exception on any error

        buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        short port= buffer.getShort(4);
        try {
            if ((buffer.getShort(6) & (short)0x0001) != 0) {
                buffer.position(4);
                addr = new byte[4];
                buffer.get(addr, 0, 4);
                ipAddress = Inet4Address.getByAddress(addr);
            } else {
                buffer.position(4);
                addr = new byte[16];
                buffer.get(addr, 0, 16);
                ipAddress = Inet6Address.getByAddress(addr);
            }
        } catch(UnknownHostException e) {
            //Can not happen, but throw exception to avoid compiler warnings
            throw new ParsingException("Invalid IP address!");
        }

        byte[] keyRaw = new byte[data.length - 12];
        buffer.position(12);
        buffer.get(keyRaw);

        try {
            key = (new ASN1InputStream(new ByteArrayInputStream(keyRaw)).readObject()).toASN1Primitive();
        } catch (IOException e) {
           throw new ParsingException("Invalid hostkey!");
        }

        return new RpsPeerParsedMessage(port, key, ipAddress);
    }
}
