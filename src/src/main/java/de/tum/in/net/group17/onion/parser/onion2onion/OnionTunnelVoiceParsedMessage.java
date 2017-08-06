package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 27.07.17.
 *
 * This class represents an ONION TUNNEL VOICE message.
 * Objects of this class may only be created by an OnionToOnionParser.
 */
public class OnionTunnelVoiceParsedMessage extends OnionToOnionParsedMessage {
    private final byte[] data;

    /**
     * Create a new ONION TUNNEL VOICE parsed message.
     * Objects of this class may only be created by an OnionToOnionParser after checking all parameters!
     *
     * @param lid The LID contained in the message.
     * @param data The voice data contained in the packet.
     */
    OnionTunnelVoiceParsedMessage(Lid lid, byte[] data) {
        super(lid);
        this.data = data;
    }

    /**
     * Get the data contained in this message.
     *
     * @return byte[] containing the data of this message.
     */
    public byte[] getData()
    {
        return data;
    }

    /**
     * @inheritDoc
     */
    @Override
    public byte[] serialize() {
        ByteBuffer buffer = buildHeader();
        buffer.put(data);
        return buffer.array();
    }

    /**
     * @inheritDoc
     */
    @Override
    public short getSize() {
        return (short)(4 + data.length);
    }

    /**
     * @inheritDoc
     */
    @Override
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_VOICE;
    }
}
