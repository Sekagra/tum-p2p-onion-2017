package de.tum.in.net.group17.onion.parser.rps;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.BeforeClass;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.05.17.
 */
public class RpsParserTestImpl {
    static RandomPeerSamplingParser prs;

    @BeforeClass
    public static void initRpsTests() {
        prs = new RandomPeerSamplingParserImpl();
    }

    /**
     * Test if the RPS parser creates the right QUERY message.
     */
    @Test
    public void testRpsQueryMessage() {
        ParsedMessage obj = prs.buildRpsQueryMsg();
        byte[] data = obj.serialize();
        byte[] compareData = {0, 4, 2, 28};

        Assert.assertArrayEquals("Parsed object contains invalid data", compareData, data);
        Assert.assertEquals(MessageType.RPS_QUERY, obj.getType());
    }

    @Test
    public void testCorrectPeerParsing() {
        int[] tmp = {
                0, 0, 2, 29, // Header
                0, 80, 0, 0, // Port and res
                127, 0, 0, 1, // IPv4 address
                0x82, 0x30, 0x22, 0x02, 0x0d, 0x30, 0x09, 0x06, 0x86, 0x2a, 0x86, 0x48, 0x0d, 0xf7, 0x01, 0x01, 0x05, 0x01, 0x03, 0x00, 0x02, 0x82, 0x00, 0x0f, 0x82, 0x30, 0x0a, 0x02, 0x82, 0x02, 0x01, 0x02, 0xaa, 0x00, 0x72, 0x84, 0x7f, 0x85, 0x23, 0xc6, 0xcc, 0x17, 0xbd, 0x2a, 0x02, 0xae, 0xeb, 0x4b, 0xbf, 0x8c, 0x05, 0xbb, 0x14, 0x05, 0x3f, 0xf8, 0xb1, 0xaf, 0xf0, 0x46, 0xc1, 0xa0, 0xf0, 0x62, 0xdf, 0x01, 0xfc, 0x0a, 0x9a, 0x15, 0x25, 0xd2, 0x1f, 0x7c, 0xaf, 0x98, 0x74, 0x9f, 0x65, 0x85, 0xaa, 0x93, 0x83, 0xe1, 0xe3, 0x7d, 0x1c, 0x8b, 0xdd, 0xfa, 0x9f, 0xd2, 0x73, 0x63, 0x1b, 0x0e, 0xcc, 0xdd, 0xfa, 0x76, 0x39, 0x71, 0x61, 0x80, 0x06, 0x8f, 0xad, 0x6f, 0xf9, 0x7a, 0x44, 0xb7, 0xed, 0x25, 0xc8, 0x64, 0x11, 0xa1, 0xc6, 0x2c, 0x78, 0x96, 0xa9, 0x19, 0xb9, 0xae, 0x9d, 0xa7, 0xb7, 0x79, 0x1a, 0x75, 0xa4, 0xf9, 0xa4, 0x25, 0x55, 0xbc, 0xdc, 0xf9, 0xc6, 0x47, 0x7e, 0x8a, 0x0f, 0x55, 0x5b, 0x83, 0x7e, 0x90, 0x9a, 0x26, 0x3c, 0x10, 0xc4, 0xf0, 0xe6, 0xdb, 0x8b, 0x87, 0xd0, 0x6c, 0x6f, 0x4d, 0x41, 0x0a, 0x52, 0x17, 0xb8, 0x92, 0x93, 0x50, 0x2c, 0x32, 0x77, 0xab, 0x8d, 0x55, 0x5b, 0xff, 0xe6, 0x23, 0xe7, 0x5d, 0xf2, 0xbf, 0xed, 0xc1, 0xd0, 0xf6, 0x9e, 0x6e, 0xa0, 0x73, 0x58, 0x46, 0x5c, 0x51, 0x42, 0x6d, 0x03, 0x86, 0x77, 0x4e, 0xcd, 0x59, 0xad, 0x72, 0xd8, 0x44, 0x26, 0x05, 0x07, 0x85, 0x9b, 0xbb, 0x43, 0xe5, 0x2e, 0xc3, 0x72, 0xdb, 0x01, 0xa0, 0xc0, 0xaa, 0x6d, 0x3d, 0x9f, 0x8a, 0xb1, 0xf7, 0x74, 0x40, 0xec, 0x1e, 0x84, 0x59, 0x6b, 0x44, 0x3f, 0x0f, 0x5b, 0xda, 0x9f, 0xaa, 0x59, 0xa0, 0xb6, 0xb0, 0x08, 0xbb, 0x4f, 0x33, 0xdf, 0x01, 0xba, 0xfa, 0x75, 0x70, 0x19, 0x13, 0xf4, 0xc2, 0x3e, 0x6a, 0x76, 0x2e, 0x78, 0x48, 0x20, 0xf1, 0x91, 0x9c, 0x84, 0x3d, 0xe5, 0x54, 0x45, 0xd3, 0x0b, 0xbc, 0x07, 0xee, 0x03, 0xeb, 0x84, 0x42, 0xae, 0x90, 0x3f, 0x51, 0x0e, 0xfa, 0x9d, 0x58, 0x4a, 0x75, 0xf6, 0x7e, 0x57, 0xaa, 0x49, 0x3e, 0x75, 0xc4, 0xdc, 0x35, 0xd7, 0x77, 0x96, 0xeb, 0xcb, 0x44, 0x28, 0x3c, 0x5c, 0x50, 0x28, 0x78, 0x1c, 0x6c, 0xb3, 0x9a, 0x24, 0x4a, 0xc2, 0x48, 0xb1, 0x19, 0x17, 0x86, 0x26, 0x85, 0xcc, 0x59, 0xcc, 0x11, 0x60, 0x4b, 0xe6, 0xd1, 0x7a, 0x73, 0x80, 0x96, 0x3c, 0xdb, 0x44, 0x36, 0x64, 0x4f, 0xdc, 0x12, 0x49, 0x67, 0x57, 0x7f, 0x98, 0x63, 0x29, 0xad, 0x3a, 0x6f, 0x6f, 0x27, 0x0a, 0xf7, 0x77, 0xc3, 0xd1, 0x0c, 0x25, 0x99, 0x68, 0xb4, 0xc0, 0x95, 0x48, 0x2a, 0x1c, 0x92, 0xa0, 0x78, 0xf2, 0x8a, 0x66, 0x0e, 0x25, 0x15, 0xc4, 0x9a, 0x95, 0x34, 0xdd, 0xaf, 0x80, 0x01, 0x29, 0x87, 0x00, 0x76, 0xbd, 0x88, 0x12, 0xff, 0xc7, 0xba, 0xa8, 0xcd, 0xd0, 0x9e, 0x9b, 0xff, 0x22, 0x45, 0x57, 0x13, 0xa0, 0xd3, 0xa5, 0xec, 0xd6, 0xd3, 0x6b, 0xaf, 0x2e, 0x45, 0x06, 0x93, 0x39, 0x6c, 0xa1, 0x7c, 0x4e, 0xce, 0x4c, 0x45, 0xa7, 0x9b, 0x7d, 0x10, 0x6f, 0x57, 0x4f, 0xd6, 0xf6, 0x3f, 0x1e, 0x16, 0xe6, 0x92, 0x98, 0xcc, 0x98, 0xc2, 0x84, 0x16, 0xc6, 0xfc, 0x72, 0xcf, 0x08, 0x39, 0x0b, 0x1c, 0xa0, 0xcd, 0x46, 0xf3, 0x03, 0x91, 0x66, 0xfb, 0xf7, 0x86, 0x2d, 0xe3, 0xbc, 0x00, 0xea, 0x5a, 0xad, 0x5a, 0x58, 0x81, 0x51, 0x81, 0x76, 0x45, 0xad, 0x35, 0xcd, 0x88, 0x32, 0x86, 0xac, 0xc6, 0x9a, 0xcd, 0xbc, 0x10, 0x1e, 0x67, 0x9d, 0xd8, 0x25, 0xb4, 0x45, 0x7b, 0x6b, 0xcb, 0x22, 0xb7, 0xb8, 0xe9, 0x50, 0xf9, 0x50, 0xf1, 0x5e, 0x86, 0xaf, 0x6b, 0x2b, 0xa9, 0xbe, 0xc9, 0x23, 0x62, 0xbb, 0x44, 0xb6, 0x67, 0x95, 0xac, 0x85, 0xcf, 0xc7, 0xd9, 0xcb, 0x5e, 0x8b, 0xd4, 0x02, 0x45, 0x01, 0x03, 0x01, 0x00
        };

        ByteBuffer data = ByteBuffer.allocate(tmp.length);
        // Java has signed bytes => To specify values > 127 we have to use this method
        // As this is only a RpsPeerParsedMessage case, performance is no issue
        data.putShort((short)tmp.length);
        for(int i = 2; i < tmp.length; ++i) {
            data.put(i, (byte)tmp[i]);
        }

        ParsedMessage obj = prs.parseMsg(data.array());
        Assert.assertArrayEquals("Parsed object contains invalid data!", data.array(), obj.serialize());
        Assert.assertEquals("Invalid message type!", MessageType.RPS_PEER, obj.getType());
    }

    @Test(expected=ParsingException.class)
    public void testParseOutgoingMessage() {
        prs.parseMsg(prs.buildRpsQueryMsg().serialize());
    }

    @Test(expected=ParsingException.class)
    public void testInvalidMessageType() {
        byte[] data = {0, 4, 1, 1}; // Message type = 0x11 = 17
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testMsgTooShortForHeader() {
        byte[] data = {0, 3, 2}; // Length too small for the packet
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testInvalidTypeield() {
        byte[] data = {0, 14, 2, 29, 1, 1, 0, 0};
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testMsgTooShortForAllPeerFields() {
        byte[] data = {0, 12, 2, 29, 1, 1, 0, 0, 127, 0, 0, 1};
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testInvalidHostKeyPeerMsg() {
        byte[] data = {0, 13, 2, 29, 1, 1, 0, 0, 127, 0, 0, 1, 1};
        prs.parseMsg(data);
    }

    // TODO: Add tests for invalid messages
}