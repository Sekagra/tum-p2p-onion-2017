package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.06.17.
 *
 * This class represents an ONION_TUNNEL_ACCEPT message.
 * Objects may only be created by an OnionToOnionParser after checking all parameters for validity.
 */
public class OnionTunnelAcceptParsedMessage extends OnionToOnionParsedMessage {
    private final byte[] authPayload;

    /**
     * Create a new ONION_TUNNEL_ACCEPT.
     * Objects of this class may only be created by an OnionToOnionParser after checking all parameters.
     *
     * @param lid The LID contained in this message.
     * @param authPayload The payload sent by the Onion Auth module for the second part of key establishment.
     */
    OnionTunnelAcceptParsedMessage(Lid lid, byte[] authPayload) {
        super(lid);
        this.authPayload = authPayload;
    }

    /**
     * Get the payload of the Onion Auth module contained in this message.
     *
     * @return A byte[] containing the Onion Auth payload.
     */
    public byte[] getAuthPayload() {
        return authPayload;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        ByteBuffer buffer = super.serializeBase();

        buffer.put(authPayload);

        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return (short)(super.getSizeBase() + (short)authPayload.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_ACCEPT;
    }
}
