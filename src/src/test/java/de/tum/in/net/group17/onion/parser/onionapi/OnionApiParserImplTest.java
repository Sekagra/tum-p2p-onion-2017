package de.tum.in.net.group17.onion.parser.onionapi;

import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import org.bouncycastle.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Assert;

import java.nio.ByteBuffer;

/**
 * Created by Marko Dorfhuber(PraMiD) on 28.05.17.
 */
public class OnionApiParserImplTest {
    private static OnionApiParser prs;
    private static byte[] derKey;

    /**
     * Create a OnionApiParser used in the RpsPeerParsedMessage cases and a byte[] containing a RpsPeerParsedMessage DER-formatted RSA key.
     */
    @BeforeClass
    public static void initTest() throws ParsingException {
        prs = new OnionApiParserImpl();

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

    @Test
    public void testBuildErrorMsg() throws ParsingException {
        byte[] expected = new byte[] {
                0x00, 0x0C, 0x02, 0x35, // Header
                0x02, 0x36, 0x00, 0x00,
                0x01, 0x01, 0x01, 0x01
        };
        ParsedMessage generated = prs.buildOnionErrorMsg(MessageType.ONION_COVER, 0x01010101);

        Assert.assertArrayEquals(expected, generated.serialize());
        Assert.assertEquals(MessageType.ONION_ERROR.getValue(), generated.getType().getValue());
    }

    @Test(expected= ParsingException.class)
    public void testInvalidRequest() throws ParsingException {
        prs.buildOnionErrorMsg(MessageType.AUTH_LAYER_DECRYPT, 0x01010101);
    }

    @Test
    public void testBuildOnionTunnelIncoming() throws ParsingException {
        ParsedMessage generated = prs.buildOnionTunnelIncomingMsg(0x01010101);
        ByteBuffer buf = ByteBuffer.wrap(generated.serialize());

        Assert.assertEquals(MessageType.ONION_TUNNEL_INCOMING.getValue(), generated.getType().getValue());
        Assert.assertEquals(generated.serialize().length, buf.getShort());
        Assert.assertEquals(0x01010101, buf.getInt(4));
    }

    @Test
    public void testBuildOnionTunnelReady() throws ParsingException {
        ParsedMessage generated = prs.buildOnionTunnelReadyMsg(0x01010101, derKey);
        ByteBuffer buf = ByteBuffer.wrap(generated.serialize());
        byte[] containedKey = new byte[derKey.length];

        Assert.assertEquals(MessageType.ONION_TUNNEL_READY.getValue(), generated.getType().getValue());
        Assert.assertEquals(generated.serialize().length, buf.getShort());
        Assert.assertEquals(0x01010101, buf.getInt(4));

        buf.position(8);
        buf.get(containedKey);
        Assert.assertArrayEquals(derKey, containedKey);
    }

    @Test(expected=ParsingException.class)
    public void testInvalidDestinationKey() throws ParsingException {
        byte[] invalidKey = new byte[derKey.length];
        Arrays.fill(invalidKey, (byte) 0);
        prs.buildOnionTunnelReadyMsg(0x01010101, invalidKey);
    }

    @Test(expected=ParsingException.class)
    public void testInvalidMessageType() throws ParsingException {
        byte[] data = {0x00, 0x04, 0x02, 0x35}; // Message type = 0x0258 = 600 => Some auth message
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testMsgTooShortForHeader() throws ParsingException {
        byte[] data = {0, 3, 2}; // Length too small for the packet
        prs.parseMsg(data);
    }

    @Test(expected=ParsingException.class)
    public void testInvalidMsgField() throws ParsingException {
        byte[] data = {0, 14, 2, 29, 1, 1, 0, 0};
        prs.parseMsg(data);
    }

    @Test
    public void testValidCoverMsg() throws ParsingException {
        byte[] data = new byte[] {
                0x00, 0x08, 0x02, 0x36,
                0x10, 0x10, 0x00, 0x00 };
        ParsedMessage parsed = prs.parseMsg(data);
        Assert.assertEquals(MessageType.ONION_COVER.getValue(), parsed.getType().getValue());
        Assert.assertArrayEquals(data, parsed.serialize());
    }

    @Test
    public void testValidDataMsg() throws ParsingException {
        byte[] data = new byte[] {
                0x00, 0x0C, 0x02, 0x34,
                0x01, 0x01, 0x01, 0x01,
                0x11, 0x11, 0x11, 0x11 };
        ParsedMessage parsed = prs.parseMsg(data);
        Assert.assertEquals(MessageType.ONION_TUNNEL_DATA.getValue(), parsed.getType().getValue());
        Assert.assertArrayEquals(data, parsed.serialize());
    }

    @Test
    public void testValidDestroyMsg() throws ParsingException {
        byte[] data = new byte[] {
                0x00, 0x08, 0x02, 0x33,
                0x01, 0x01, 0x01, 0x01 };
        ParsedMessage parsed = prs.parseMsg(data);
        Assert.assertEquals(MessageType.ONION_TUNNEL_DESTROY.getValue(), parsed.getType().getValue());
        Assert.assertArrayEquals(data, parsed.serialize());
    }

    @Test
    public void testValidBuildMsg() throws ParsingException {
        byte[] headIp = new byte[] {
                0x00, 0x08, 0x02, 0x30,
                0, 0, 0, 80,
                127, 0, 0, 1
        };

        byte[] data = Arrays.concatenate(headIp, derKey);
        data[0] = (byte)(data.length >> 8);
        data[1] = (byte)data.length;
        ParsedMessage parsed = prs.parseMsg(data);
        Assert.assertEquals(MessageType.ONION_TUNNEL_BUILD.getValue(), parsed.getType().getValue());
        Assert.assertArrayEquals(data, parsed.serialize());
    }
}
