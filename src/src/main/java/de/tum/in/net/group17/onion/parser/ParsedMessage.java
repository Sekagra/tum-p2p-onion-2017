package de.tum.in.net.group17.onion.parser;

import java.nio.ByteBuffer;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class ParsedMessage {
    private byte[] data;
    private MessageType type;

    protected ParsedMessage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    public MessageType getType() {
        if(this.type == null) {
            ByteBuffer buffer = ByteBuffer.wrap(this.data);
            buffer.position(2);
            this.type = MessageType.values()[buffer.getShort()];
        }
        return type;
    }
}
