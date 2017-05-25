package de.tum.in.net.group17.onion.parser.rps;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.05.17.
 *
 * This class represents a packet already processed by the parser.
 * Using this object the RandomPeerSamplingParser marks this object as valid after parsing it.
 * The user is not allow to create a RpsParsedObject itself.
 */
public class RpsParsedObject {

    /**
     * The actual packet.
     */
    private byte[] data;
    private RPS_MSG_TYPE type;

    /**
     * Create a new RpsParsedObject.
     *
     * @param packet The actual packet content.
     * @param type The type of this message.
     */
    protected RpsParsedObject(byte[] packet, RPS_MSG_TYPE type) {
        if (packet == null)
            throw new IllegalArgumentException("The packet array must not be null!");
        this.data = packet;
        this.type = type;
    }

    /**
     * Get the content of this parsed packet.
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
    public RPS_MSG_TYPE getType() {
        return type;
    }

    public enum RPS_MSG_TYPE {
        RPS_QUERY(540),
        RPS_PEER(541);

        private int val;

        RPS_MSG_TYPE(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }
}
