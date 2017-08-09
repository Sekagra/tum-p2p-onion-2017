package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;

/**
 * Created by Marko Dorfhuber(PraMiD) on 08.08.17.
 *
 * Represents a ONION TUNNEL ESTABLISHED message.
 * Objects of this type shall only be created by an Onion Parser after checking all parameters.
 */
public class OnionTunnelEstablishedParsedMessage extends OnionToOnionParsedMessage {
    /**
     * Create a net ONION TUNNEL ESTABLISH message for the given LID.
     * Objects of this type shall only be created by OnionToOnion Parsers after checking all parameters.
     *
     * @param lid The LID of the fully established tunnel on the last hop.
     */
    OnionTunnelEstablishedParsedMessage(Lid lid) {
        super(lid);
    }

    /**
     * @inheritDoc
     */
    @Override
    public byte[] serialize() {
        return super.serializeBase().array();
    }

    /**
     * @inheritDoc
     */
    @Override
    public short getSize() {
        return super.getSizeBase();
    }

    /**
     * @inheritDoc
     */
    @Override
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_ESTABLISHED;
    }
}
