package de.tum.in.net.group17.onion.parser.onion;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.05.17.
 *
 * This class is created by an Onion Parser for encapsulating created outgoing messages or to mark a packet as
 * valid after parsing.
 */
public class OnionParsedObject {
    /**
     * The actual packet.
     */
    private byte[] data;
    private ONION_MSG_TYPE type;

    /**
     * Create a new OnionParsedObject.
     *
     * @param packet The actual packet content.
     * @param type The type of this message.
     */
    protected OnionParsedObject(byte[] packet, ONION_MSG_TYPE type) {
        if (packet == null)
            throw new IllegalArgumentException("The packet array must not be null!");
        this.data = packet;
        this.type = type;
    }

    /**
     * Get the content of the parsed packet.
     *
     * @return A byte array containing the actual content.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get the message type of this parsed message.
     *
     * @return The message type.
     */
    public ONION_MSG_TYPE getType() {
        return type;
    }

    public enum ONION_MSG_TYPE {
        ONION_TUNNEL_BUILD(560),
        ONION_TUNNEL_READY(561),
        ONION_TUNNEL_INCOMING(562),
        ONION_TUNNEL_DESTROY(563),
        ONION_TUNNEL_DATA(564),
        ONION_ERROR(565),
        ONION_COVER(566);

        private int val;

        ONION_MSG_TYPE(int val) {
            this.val = val;
        }

        public int getValue() {
            return val;
        }
    }
}
