package de.tum.in.net.group17.onion.parser.rps;

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
public class RandomPeerSamplingParserImpl implements RandomPeerSamplingParser {

    /**
     * Build a RPS Query message.
     * The method throws and IllegalArgumentException on every parse error!
     *
     * @return The RpsParsedObject containing the RPS Query message.
     */
    public RpsParsedObject buildRpsQueryMsg() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putShort((short)4);
        buffer.putShort((short)RpsParsedObject.RPS_MSG_TYPE.RPS_QUERY.getValue());

        return new RpsParsedObject(buffer.array(), RpsParsedObject.RPS_MSG_TYPE.RPS_QUERY);
    }

    /**
     * Parse an incoming RPS message.
     * The method throws and IllegalArgumentException on every parse error!
     *
     * @param data Incoming packet.
     * @return RpsParsedObject containing the message type and packet data if parsing succeeds.
     */
    public RpsParsedObject parseMsg(byte[] data) {
        if (data == null || data.length < 4) // Null or shorter than the header?
            throw new IllegalArgumentException("The package must at least contain the header!");

        if(data.length > 65536)
            throw new IllegalArgumentException("Packet too long!");

        ByteBuffer buffer = ByteBuffer.wrap(data);
        if((int)buffer.getShort(0) != data.length)
            throw new IllegalArgumentException("Package size does not match size field in header!");

        RpsParsedObject.RPS_MSG_TYPE msgType;
        try {
            short t = buffer.getShort(2);
            msgType = RpsParsedObject.RPS_MSG_TYPE.values()[buffer.getShort(2)];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unknown message type: " + buffer.getShort(0) + "!");
        }

        switch(msgType) {
            case RPS_PEER:
                return parseRpsPeerMsg(data);
            default:
                throw new IllegalArgumentException("Not able to parse message. Type: " + msgType.getValue() + "!");
        }
    }

    /**
     * Parse an RPS PEER message.
     * The method throws an IllegalArgumentException on every parsing error!
     *
     * @param data Array containing the packet to parse.
     * @return RpsParsedObject of type RPS_MSG_TYPE.RPS_PEER if the packet is a valid RPS PEER message.
     */
    private RpsParsedObject parseRpsPeerMsg(byte[] data) {
        if (data.length < 13) // Contains header, port, res, IP address (IPv4 here!) and key (No length known)
            throw new IllegalArgumentException("Packet is too short to contain a header, an IP and a hostkey!");

        ByteBuffer buffer = ByteBuffer.wrap(data);

        // TODO: We assume to have IPv4 addresses -> Change to handle both v4 and v6 if we know how to do this
        // TODO: Remove check if valid IP -> What else with 4 byte..?
        byte[] address = new byte[4];
        buffer.get(address, 8, 4);

        try {
            InetAddress.getByAddress(address);
        } catch(UnknownHostException e) {
            throw new IllegalArgumentException("Cannot parse the IP address!");
        }

        byte[] key = new byte[data.length - 12];
        buffer.get(key, 12, data.length - 12);

        try {
            new ASN1InputStream(new ByteArrayInputStream(key)).readObject();
        } catch (IOException e) {
           throw new IllegalArgumentException("Invalid hostkey!");
        }

        return new RpsParsedObject(data, RpsParsedObject.RPS_MSG_TYPE.RPS_PEER);
    }
}
