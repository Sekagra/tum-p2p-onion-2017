package de.tum.in.net.group17.onion.parser;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class ParsedMessage {
    private byte[] data;

    public ParsedMessage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }
}
