package de.tum.in.net.group17.onion.parser.rps;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import org.bouncycastle.util.Arrays;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.BeforeClass;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.05.17.
 */
public class RpsParserImplTest {
    static RandomPeerSamplingParser prs;
    static byte[] derKey;

    @BeforeClass
    public static void initTest() {
        prs = new RandomPeerSamplingParserImpl();

        // Create a key in DER format used in different cases
        int[] tmp = {
                0x30, 0x82, 0x01, 0x22, 0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d, 0x01, 0x01,
                0x01, 0x05, 0x00, 0x03, 0x82, 0x01, 0x0f, 0x00, 0x30, 0x82, 0x01, 0x0a, 0x02, 0x82, 0x01, 0x01,
                0x00, 0xcd, 0x23, 0x7f, 0xd7, 0x3a, 0x20, 0xb5, 0x6b, 0x4e, 0x70, 0xc0, 0x0b, 0xd5, 0x07, 0xa7,
                0x8d, 0x94, 0x25, 0x99, 0x7d, 0x65, 0xe7, 0xbe, 0xe8, 0x41, 0x69, 0x45, 0x83, 0xde, 0x60, 0xbe,
                0xe2, 0xf8, 0x31, 0x79, 0xc0, 0x3f, 0x73, 0xd9, 0x72, 0xa7, 0xbe, 0xd4, 0x43, 0x69, 0x07, 0x79,
                0x33, 0xc2, 0x08, 0x53, 0x0b, 0x33, 0xbb, 0x59, 0xcd, 0x4e, 0x96, 0xe2, 0x32, 0x24, 0xaf, 0xc3,
                0xf1, 0xd7, 0xd5, 0xf9, 0x33, 0xdf, 0xa9, 0x4b, 0xda, 0x88, 0x6e, 0xdf, 0xa6, 0xe3, 0x5d, 0x2e,
                0xf2, 0x7d, 0xa7, 0xcf, 0xce, 0x65, 0x22, 0xf1, 0x7f, 0x20, 0x3c, 0x4d, 0x01, 0x34, 0xb8, 0x67,
                0xb6, 0xea, 0x42, 0xcc, 0xe5, 0x02, 0xe0, 0x2f, 0x10, 0x79, 0x8e, 0x58, 0x44, 0xd8, 0x02, 0x51,
                0x8d, 0xeb, 0x48, 0xcb, 0xad, 0xb8, 0xd4, 0xd6, 0xa5, 0xcf, 0x7e, 0x4d, 0x91, 0x1a, 0x6c, 0x2c,
                0x40, 0xce, 0x3e, 0x15, 0x0c, 0x6c, 0x86, 0xb0, 0x96, 0xd1, 0x40, 0x14, 0x2e, 0x9c, 0xd7, 0x23,
                0x33, 0x2f, 0x6d, 0xb2, 0xec, 0xc6, 0x67, 0x64, 0x34, 0xbe, 0xa9, 0xd6, 0x58, 0xe8, 0x79, 0xa1,
                0x4e, 0x71, 0x7d, 0x42, 0xef, 0x27, 0x9b, 0xc9, 0xc1, 0xf1, 0x88, 0xc2, 0xf5, 0x8c, 0x45, 0xda,
                0x17, 0x99, 0x02, 0xc3, 0x33, 0x93, 0x22, 0xe1, 0x9a, 0x36, 0xde, 0x6e, 0x37, 0xd2, 0x72, 0x1b,
                0xa9, 0xb1, 0xda, 0x85, 0x0b, 0x08, 0xf6, 0x14, 0x12, 0x0d, 0x39, 0xf7, 0xbc, 0xd7, 0xb5, 0xa2,
                0x7f, 0x80, 0x6a, 0x46, 0x0d, 0x55, 0x05, 0x21, 0x55, 0x71, 0xa3, 0x78, 0xe3, 0xfb, 0xf8, 0x5e,
                0xa0, 0xfb, 0xc3, 0x5d, 0x9b, 0x20, 0xd4, 0xf4, 0xff, 0xbc, 0x85, 0xe9, 0x8b, 0x32, 0x19, 0xa1,
                0xb4, 0xb8, 0xe2, 0x0d, 0xdd, 0xe3, 0xde, 0xfc, 0x51, 0x33, 0xfe, 0x72, 0x61, 0x93, 0x80, 0x42,
                0xbd, 0x02, 0x03, 0x01, 0x00, 0x01
        };

        derKey = new byte[tmp.length];
        // Java has signed bytes => To specify values > 127 we have to use this method
        // As this is only a RpsPeerParsedMessage case, performance is no issue
        for(int i = 0; i < tmp.length; ++i) {
            derKey[i] = (byte)tmp[i];
        }
    }

    /**
     * Test if the RPS parser creates the right QUERY message.
     */
    @Test
    public void testRpsQueryMessage() throws ParsingException {
        ParsedMessage obj = prs.buildRpsQueryMsg();
        byte[] data = obj.serialize();
        byte[] compareData = {0, 4, 2, 28};

        Assert.assertArrayEquals("Parsed object contains invalid data", compareData, data);
        Assert.assertEquals(MessageType.RPS_QUERY, obj.getType());
    }

    @Test
    public void testCorrectPeerParsing()  throws ParsingException {
        int[] headerRaw = {
                0x1, 0x32, 0x2, 0x1D, // Header
                0x0, 0x80, 0x0, 0x0, // Port and res
                0x127, 0x0, 0x0, 0x1 // IPv4 address
        };

        ByteBuffer headerData = ByteBuffer.allocate(headerRaw.length);
        // Java has signed bytes => To specify values > 127 we have to use this method
        // As this is only a RpsPeerParsedMessage case, performance is no issue
        headerData.putShort((short)headerRaw.length);
        for(int i = 0; i < headerRaw.length; ++i) {
            headerData.put(i, (byte)headerRaw[i]);
        }

        byte[] testData = Arrays.concatenate(headerData.array(), derKey);

        ParsedMessage obj = prs.parseMsg(testData);
        Assert.assertArrayEquals("Parsed object contains invalid data!", testData, obj.serialize());
        Assert.assertEquals("Invalid message type!", MessageType.RPS_PEER, obj.getType());
    }

    @Test(expected=ParsingException.class)
    public void testParseOutgoingMessage() throws ParsingException {
        prs.parseMsg(prs.buildRpsQueryMsg().serialize());
    }

    @Test(expected=ParsingException.class)
    public void testInvalidMessageType() throws ParsingException {
        byte[] data = {0, 4, 1, 1}; // Message type = 0x11 = 17
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testMsgTooShortForHeader() throws ParsingException {
        byte[] data = {0, 3, 2}; // Length too small for the packet
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testInvalidTypeield()  throws ParsingException {
        byte[] data = {0, 14, 2, 29, 1, 1, 0, 0};
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testMsgTooShortForAllPeerFields() throws ParsingException {
        byte[] data = {0, 12, 2, 29, 1, 1, 0, 0, 127, 0, 0, 1};
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testInvalidHostKeyPeerMsg() throws ParsingException {
        byte[] data = {0, 13, 2, 29, 1, 1, 0, 0, 127, 0, 0, 1, 1};
        prs.parseMsg(data);
    }
}
