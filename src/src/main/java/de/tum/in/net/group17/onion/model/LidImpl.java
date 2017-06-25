package de.tum.in.net.group17.onion.model;

import java.util.Arrays;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.06.17.
 *
 * This class implements the Lid interface and represents the local identifier
 * in the first version of the application.
 */
public class LidImpl implements Lid {
    private static final short lidLen = 16;
    private byte[] data;

    private LidImpl(byte[] data) {
        this.data = data;
    }

    /**
     * @inheritDoc
     */
    public byte[] serialize() {
        return data;
    }

    /**
     * Deserialize a Local Identifier.
     *
     * @param rawLid A new Lid object containing the data from the raw byte[].
     */
    public static Lid deserialize(byte[] rawLid) {
        if(rawLid == null || rawLid.length != (int)lidLen)
            throw new IllegalArgumentException("Illegal raw lid. Cannot deserialize!");
        return new LidImpl(rawLid);
    }

    /**
     * @inheritDoc
     */
    public short getSize() {
        return lidLen;
    }

    /**
     * Possibility to check equality with this LidImpl object.
     *
     * @param other The object to compare with.
     * @return True if other is a LidImpl object and contains the same data;
     */
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof LidImpl))
            return false;
        return Arrays.equals(this.data, ((LidImpl)other).data);
    }
}
