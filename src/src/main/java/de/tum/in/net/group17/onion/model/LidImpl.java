package de.tum.in.net.group17.onion.model;

import com.google.inject.Inject;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.06.17.
 *
 * This class implements the Lid interface and represents the local identifier
 * in the first version of the application.
 */
public class LidImpl implements Lid {
    public static final short LENGTH = 16;
    private byte[] data;

    /**
     * Create a new LID as used in version 1.0 of this onion module.
     *
     *
     * @param data The LID encoded as byte[].
     */
    private LidImpl(byte[] data) {
        this.data = data;
    }

    /**
     * @inheritDoc
     */
    @Override
    public byte[] serialize() {
        return data;
    }

    /**
     * Deserialize a Local Identifier.
     *
     *
     * @param rawLid A new Lid object containing the data from the raw byte[].
     *
     * @return The deserialized Lid object.
     */
    public static Lid deserialize(byte[] rawLid) {
        if(rawLid == null || rawLid.length != (int) LENGTH)
            throw new IllegalArgumentException("Illegal raw lid. Cannot deserialize!");
        return new LidImpl(rawLid);
    }

    /**
     * @inheritDoc
     */
    @Override
    public short getSize() {
        return LENGTH;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof LidImpl))
            return false;
        return Arrays.equals(this.data, ((LidImpl)other).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    /**
     * Creates a new random LID to be used for identifying a new TunnelSegment.
     * @return A new random LID.
     */
    public static Lid createRandomLid() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return new LidImpl(bb.array());
    }
}
