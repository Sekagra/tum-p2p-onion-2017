package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 04.06.17.
 */
public class OnionTunnelBuildParsedMessage extends ParsedMessage {
    private final short port;
    private final InetAddress ipAddress;
    private final ASN1Primitive destinationKey;

    /**
     * Create a new ONION TUNNEL BUILD message.
     * This message may only be created by an OnionParser after checking the parameters.
     *
     * @param port The port which should be used for the onion connection.
     * @param address The address of the destination peer.
     * @param dstKey The key of the destination peer.
     */
    protected OnionTunnelBuildParsedMessage(short port, InetAddress address, ASN1Primitive dstKey) {
        this.port = port;
        this.ipAddress = address;
        this.destinationKey = dstKey;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.putShort((short)0);
        buffer.putShort(port);
        buffer.put(ipAddress.getAddress());
        buffer.put(getRawKey());

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(8 + getRawKey().length + (isIpv4() ? 4 : 16));
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_BUILD;
    }


    /**
     * Get the port used for the onion connection.
     *
     * @return The used port.
     */
    public short getPort() {
        return port;
    }

    /**
     * Get the IP address of the other peer.
     *
     * @return InetAddress used by the other peer.
     */
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    /**
     * Get the destination key of the other peer.
     *
     * @return The ASN1Primitive containing the key of the other peer.
     */
    public ASN1Primitive getDestinationKey() {
        return destinationKey;
    }

    /**
     * Return if this message contains an IPv4 address.
     *
     * @return true, if the message contains an IPv4 address.
     */
    public boolean isIpv4() {
        return ipAddress instanceof Inet4Address;
    }

    /**
     * Return the destination key in a byte[].
     * Method throws an Error if an invalid destination key was passed to this object.
     *
     * @return byte[] containing the destination key.
     */
    private byte[] getRawKey() {
        try {
            return destinationKey.getEncoded();
        } catch(IOException e) {
            // Should be checked beforehand!
            throw new Error("Invalid destination key!");
        }
    }
}
