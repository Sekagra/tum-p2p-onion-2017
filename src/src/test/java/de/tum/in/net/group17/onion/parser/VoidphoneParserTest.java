package de.tum.in.net.group17.onion.parser;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class VoidphoneParserTest {
    @org.junit.Test
    public void checkSize() throws Exception {
        byte[] sample = new byte[] {0, 8, 0, 0, 0, 0, 127, 127};
        VoidphoneParser parser = new VoidphoneParser();
        assert parser.checkSize(sample);
    }

    @org.junit.Test
    public void checkType() throws Exception {
        byte[] sample = new byte[] {0, 8, 2, 88, 0, 0, 0, 0};
        VoidphoneParser parser = new VoidphoneParser();
        assert parser.checkType(sample, MessageType.AUTH_SESSION_START);
    }
}