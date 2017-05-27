package de.tum.in.net.group17.onion.parser;

import org.junit.Test;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class VoidphoneParserTest {
    @Test
    public void checkSize() throws Exception {
        byte[] sample = new byte[] {0, 8, 0, 0, 0, 0, 127, 127};
        VoidphoneParser parser = new VoidphoneParser();
        assert parser.checkSize(sample);
    }

    @Test
    public void checkType() throws Exception {
        byte[] sample = new byte[] {0, 8, 2, 88, 0, 0, 0, 0};
        VoidphoneParser parser = new VoidphoneParser();
        assert parser.checkType(sample, MessageType.AUTH_SESSION_START);
    }
}