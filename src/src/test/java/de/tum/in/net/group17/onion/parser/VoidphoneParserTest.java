package de.tum.in.net.group17.onion.parser;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by Christoph Rudolf on 25.05.17.
 * Additional RpsPeerParsedMessage cases by Marko Dorfhuber(PraMiD) on 28.05.17.
 */
public class VoidphoneParserTest {
    private static VoidphoneParser prs;

    @BeforeClass
    public static void initTests() {
        prs = new VoidphoneParser() {
        };
    }


    /*
     * Test cases for VoiphoneParser.checkSize
     */
    @Test
    public void testCheckSizeValid() throws ParsingException {
        byte[] sample = new byte[] {0, 8, 0, 0, 0, 0, 127, 127};
        prs.checkSize(sample); // Must no throw an exception
    }
    @Test(expected=ParsingException.class)
    public void testTooShortMessage() throws ParsingException {
        byte[] sample = new byte[] {0, 3, 2};
        prs.checkSize(sample);
    }

    @Test(expected=ParsingException.class)
    public void testTooLargeMessage() throws ParsingException {
        byte[] sample = new byte[65536];
        prs.checkSize(sample);
    }

    @Test(expected=ParsingException.class)
    public void checkInvalidSizeInPacket() throws ParsingException {
        byte[] sample = new byte[] {0, 5, 1, 2};
        prs.checkSize(sample);
    }


    /*
     * Test cases for VoidphoneParser.checkType
     */
    @Test
    public void testCheckExpectedType() throws ParsingException {
        byte[] sample = new byte[] {0, 8, 2, 88, 0, 0, 0, 0};
        prs.checkType(sample, MessageType.AUTH_SESSION_START); // Must not throw an exception
    }

    @Test(expected=ParsingException.class)
    public void testCheckUnexpectedType() throws ParsingException {
        byte[] sample = new byte[] {0, 8, 2, 88, 0, 0, 0, 0};
        prs.checkType(sample, MessageType.AUTH_SESSION_CLOSE);
    }
}