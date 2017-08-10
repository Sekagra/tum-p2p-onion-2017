package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.06.17.
 *
 * This class represents a ONION_TUNNEL_TEARDOWN message.
 * Objects of this class may only be created by OnionToOnionParsers.
 */
public class OnionTunnelTeardownParsedMessage extends OnionToOnionParsedMessage {
    /**
     * Create a new ONION_TUNNEL_TEARDOWN message after checking all parameters.
     * This object may only be created by a OnionToOnionParser.
     *
     * @param incomingLid The LID of the tunnel between the initiator and the receiver.
     */
    OnionTunnelTeardownParsedMessage(Lid incomingLid) {
        super(incomingLid);
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        return super.serializeBase().array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return super.getSizeBase();
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_TEARDOWN;
    }
}
