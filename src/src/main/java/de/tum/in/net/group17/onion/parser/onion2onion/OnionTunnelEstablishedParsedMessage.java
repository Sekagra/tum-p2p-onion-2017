package de.tum.in.net.group17.onion.parser.onion2onion;

import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.model.LidImpl;
import de.tum.in.net.group17.onion.parser.MessageType;
import sun.plugin.dom.exception.InvalidStateException;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 08.08.17.
 *
 * Represents a ONION TUNNEL ESTABLISHED message.
 * Objects of this type shall only be created by an Onion Parser after checking all parameters.
 */
public class OnionTunnelEstablishedParsedMessage extends OnionToOnionParsedMessage {
    private Lid lidOld;

    /**
     * Create a new ONION TUNNEL ESTABLISHED message for a new tunnel.
     * Objects of this type shall only be created by an OnionToOnion parser.
     *
     * @param lidNew The Lid of the newly established tunnel.
     */
    OnionTunnelEstablishedParsedMessage(Lid lidNew) {
        this(lidNew, null);
    }

    /**
     * Create a new ONION TUNNEL ESTABLISHED used to switch from an old tunnel to a new one.
     * Objects of this type shall only be created by an OnionToOnion parser.
     *
     * @param lidNew Lid of the new tunnel.
     * @param lidOld Lid of the old tunnel.
     */
    OnionTunnelEstablishedParsedMessage(Lid lidNew, Lid lidOld) {
        super(lidNew);
        this.lidOld = lidOld;
    }

    /**
     * Returns true if this ONION TUNNEL ESTABLISHED message is used in a tunnel refresh.
     *
     * @return true if used for tunnel refresh.
     */
    public boolean isRefresh() {
        return lidOld != null;
    }

    /**
     * Get the old LID in the tunnel refresh message.
     *
     * @return The LID of the refreshed tunnel.
     */
    public Lid getLidOld() {
        if(!isRefresh())
            throw new InvalidStateException("Message not used for tunnel refresh.");
        return this.lidOld;
    }

    /**
     * @inheritDoc
     */
    @Override
    public byte[] serialize() {
        ByteBuffer buf = super.serializeBase();
        if(lidOld != null)
            buf.put(lidOld.serialize());
        return buf.array();
    }

    /**
     * @inheritDoc
     */
    @Override
    public short getSize() {
        return (short)(super.getSizeBase() + (lidOld == null ? 0 : LidImpl.LENGTH));
    }

    /**
     * @inheritDoc
     */
    @Override
    public MessageType getType() {
        return MessageType.ONION_TUNNEL_ESTABLISHED;
    }
}
