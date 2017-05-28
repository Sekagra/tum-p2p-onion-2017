package de.tum.in.net.group17.onion.parser;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class VoidphoneParserTest {
    private static VoidphoneParser prs;

    @BeforeClass
    public static void initTests() {
        prs = new VoidphoneParser();
    }

    @Test
    public void checkSize() throws Exception {
        byte[] sample = new byte[] {0, 8, 0, 0, 0, 0, 127, 127};
        prs.checkSize(sample); // Must no throw an exception
    }

    @Test
    public void checkType() {
        byte[] sample = new byte[] {0, 8, 2, 88, 0, 0, 0, 0};
        VoidphoneParser parser = new VoidphoneParser();
        prs.checkType(sample, MessageType.AUTH_SESSION_START); // Must not throw an exception
    }

    @Test(expected=de.tum.in.net.group17.onion.parser.ParsingException.class) // I do not know why i have to do this
    public void checkTypeUnexpected() {
        byte[] sample = new byte[] {0, 8, 2, 88, 0, 0, 0, 0};
        VoidphoneParser parser = new VoidphoneParser();
        prs.checkType(sample, MessageType.AUTH_SESSION_CLOSE);
    }
}