package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.06.17.
 *
 * This class encapsulates the fields used by all messages in the OnionToOnion protocol.
 */
public abstract class OnionToOnionParsedMessage extends ParsedMessage {
    protected Lid incomingLid;

    /**
     * Create a new OnionToOnionParsedMessage.
     * This constructor may only be called by the specific subclasses.
     *
     * @param incomingLid The LID of the tunnel this message was received from.
     */
    OnionToOnionParsedMessage(Lid incomingLid) {
        this.incomingLid = incomingLid;
    }

    /**
     * Get the LID contained in this message.
     * This is the LID of the incoming tunnel from the receivers perspective.
     *
     * @return A Lid object representing the local identifier used in this message.
     */
    public Lid getLid() {
        return incomingLid;
    }

    /**
     * Serialize the header and the incoming tunnel LID.
     *
     * @return A ByteBuffer containing the header and incoming tunnel LID of this message.
     */
    public ByteBuffer serializeBase() {
        ByteBuffer buffer = buildHeader();

        buffer.put(incomingLid.serialize());

        return buffer;
    }

    /**
     * This method only returns the size that equal for all OnionToOnion messages. (Header and incoming LID)
     *
     * @return The size of the message header and incoming tunnel lid all OnionToOnion messages have in common.
     */
    public short getSizeBase() {
        return (short)(4 + incomingLid.getSize());
    }
}
