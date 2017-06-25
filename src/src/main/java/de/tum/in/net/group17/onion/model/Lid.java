package de.tum.in.net.group17.onion.model;

/**
 * Created by Marko Dorfhuber(PraMiD) on 24.06.17.
 *
 * This interface defines methods to deal with LIDs in the OnionProtocol.
 */
public interface Lid {
    /**
     * Serialize this Local Identifier.
     *
     * @return A byte[] representing this local identifier.
     */
    byte[] serialize();

    /**
     * Get the size of this Lid object if converted to a= byte[].
     *
     * @return The size if converte to a byte[].
     */
    short getSize();
}
