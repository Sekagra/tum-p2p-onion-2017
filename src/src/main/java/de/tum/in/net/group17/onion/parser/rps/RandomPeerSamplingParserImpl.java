package de.tum.in.net.group17.onion.parser.rps;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.05.17.
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
        buffer.putShort((short)RpsParsedObject.RPS_MSG_TYPE.RPS_QUERY.getVal());

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
            msgType = RpsParsedObject.RPS_MSG_TYPE.values()[buffer.getShort(2)];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unknown message type: " + buffer.getShort(0) + "!");
        }

        return parseRpsPeer(data);
    }

    /**
     * Parse an RPS PEER message.
     * The method throws an IllegalArgumentException on every parsing error!
     *
     * @param data Array containing the packet to parse.
     * @return RpsParsedObject of type RPS_MSG_TYPE.RPS_PEER if the packet is a valid RPS PEER message.
     */
    private RpsParsedObject parseRpsPeer(byte[] data) {
        if (data.length < 4 + 4 + 1) // Contains header, IP address (IPv4 here!) and key (No length known)
            throw new IllegalArgumentException("Packet is too short to contain a header, an IP and a hostkey!");

        ByteBuffer buffer = ByteBuffer.wrap(data);

        //TODO: We assume to have IPv4 addresses -> Change to handle both v4 and v6 if we know how to do this
        byte[] address = new byte[4];
        buffer.get(address, 4, 4);

        try {
            InetAddress.getByAddress(address);
        } catch(UnknownHostException e) {
            throw new IllegalArgumentException("Cannot parse the IP address!");
        }

        byte[] key = new byte[data.length - 8];
        buffer.get(key, 8, data.length - 8);

        try {
            ASN1Primitive inputStream = new ASN1InputStream(new ByteArrayInputStream(key)).readObject();
        } catch (IOException e) {
           throw new IllegalArgumentException("Invalid hostkey!");
        }

        return new RpsParsedObject(data, RpsParsedObject.RPS_MSG_TYPE.RPS_PEER);
    }
}
