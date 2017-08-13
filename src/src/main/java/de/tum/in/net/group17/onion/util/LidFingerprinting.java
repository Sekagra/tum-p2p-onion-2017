package de.tum.in.net.group17.onion.util;

import de.tum.in.net.group17.onion.model.Lid;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Marko Dorfhuber(PraMiD) on 13.08.17.
 *
 * Provides a method to generate a short fingerprint of a LID.
 */
public class LidFingerprinting {
    /**
     * Get a small fingerprint from an existing LID.
     *
     * @param rawLid The LID to fingerprint in byte[] representation.
     * @return The fingerprint.
     */
    public static int fingerprint(byte[] rawLid) {
        ByteBuffer wrapped = ByteBuffer.wrap(Arrays.copyOfRange(rawLid, 0, 4));
        return wrapped.getInt();
    }
}
