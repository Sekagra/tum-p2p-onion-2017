package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.06.17.
 *
 * This class represents an ONION_TUNNEL_INIT message in the first version of the application.
 * Objects may only be created by an OnionToOnionParser after checking all parameters.
 */
public class OnionTunnelInitParsedMessage extends OnionToOnionParsedMessage {
    private final byte[] authPayload;

    /**
     * Create a new OnionTunnelInitParsedMessage after checking all parameters.
     * Objects of this class may only be created by OnionToOnionParsers.
     *
     * @param lid The LID contained in the message.
     * @param authPayload The payload of the Onion Auth module contained in this message.
     */
    OnionTunnelInitParsedMessage(Lid lid, byte[] authPayload) {
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
        return (short)(super.getSizeBase() + authPayload.length);
    }

    /**
     * @inheritDoc
     */
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_INIT;
    }
}
