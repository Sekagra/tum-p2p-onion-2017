package de.tum.in.net.group17.onion.parser;

import sun.plugin2.message.Message;

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
            this.type = MessageType.valueOf(buffer.getShort(2));
        }
        return type;
    }
}
