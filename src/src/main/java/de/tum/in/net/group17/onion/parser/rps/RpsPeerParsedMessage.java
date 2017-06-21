package de.tum.in.net.group17.onion.parser.rps;

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
public class RpsPeerParsedMessage extends ParsedMessage {
    private final ASN1Primitive key;
    private final InetAddress ipAddress;
    private final short port;

    /**
     * Create a new RPS PEER message from the given parameters.
     * This object may only be created from a corresponding parser after checking all parameters in the package.
     *
     * @param port The port used for this peer.
     * @param key Key contained in the packet.
     * @param addr Address contained in the packet.
     */
    protected RpsPeerParsedMessage(short port, ASN1Primitive key, InetAddress addr) {
        this.key = key;
        this.ipAddress = addr;
        this.port = port;
    }

    /**
     * @inhertDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.put(ipAddress.getAddress());
        buffer.put(getKeyRaw());

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        short ipLen = (short)(isIpv4() ? 4 : 16);
        return (short) (4 + getKeyRaw().length + ipLen);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.RPS_PEER;
    }

    /**
     * Get the DER key stored in this packet as byte[]
     *
     * @return Key as byte[]
     */
    private byte[] getKeyRaw() {
        try {
            return key.getEncoded();
        } catch(IOException e) {
            //Message was parsed beforehand
        }
        return null;
    }

    /**
     * Return the port contained in this packet.
     *
     * @return The port contained in this packet.
     */
    public short getPort() {
        return port;
    }

    /**
     * Return the key stored in this packet.
     *
     * @return DER formatted key.
     */
    public ASN1Primitive getKey() {
        return this.key;
    }

    /**
     * Return the IP address contained in this packet.
     *
     * @return InetAddress representing the IP address in this packet.
     */
    public InetAddress getAddress() {
        return this.ipAddress;
    }

    /**
     * Return if this message contains an IPv4 address.
     *
     * @return true, if this message contains an IPv4 address.
     */
    public boolean isIpv4() {
        return ipAddress instanceof Inet4Address;
    }
}
