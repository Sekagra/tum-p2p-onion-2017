package de.tum.in.net.group17.onion.parser;

import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParser;
import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParserImpl;
import de.tum.in.net.group17.onion.parser.rps.RpsParsedObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Before;

import java.util.Arrays;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.05.17.
 */
public class TestRpsParser {
    RandomPeerSamplingParser prs;

    @Before
    public void initRpsTests() {
        prs = new RandomPeerSamplingParserImpl();
    }

    /**
     * Test if the RPS parser creates the right QUERY message.
     */
    @Test
    public void testRpsQueryMessage() {
        RpsParsedObject obj = prs.buildRpsQueryMsg();
        byte[] data = obj.getData();
        byte[] compareData = {0, 4, 2, 28};

        Assert.assertArrayEquals("Parsed object contains invalid data", compareData, data);
        Assert.assertEquals(RpsParsedObject.RPS_MSG_TYPE.RPS_QUERY, obj.getType());
    }

    @Test
    @Ignore("Have to add DER data")
    public void testCorrectPeerParsing() {
        byte[] data = {
                0, 1, 2, 29, // Header
                0, 80, 0, 0, // Port and res
                127, 0, 0, 1, // IPv4 address
        };

        // TODO: Fix this test case after merging
        RpsParsedObject obj = prs.parseMsg(data);
        Assert.assertArrayEquals("Parsed object contains invlaid data!", data, obj.getData());
        Assert.assertEquals("Invalid message type!", RpsParsedObject.RPS_MSG_TYPE.RPS_PEER, obj.getType());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParseOutgoingMessage() {
        prs.parseMsg(prs.buildRpsQueryMsg().getData());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidMessageType() {
        byte[] data = {0, 4, 1, 1}; // Message type = 0x11 = 17
        prs.parseMsg(data);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMsgTooShortForHeader() {
        byte[] data = {0, 3, 2}; // Length too small for the packet
        prs.parseMsg(data);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidMsgField() {
        byte[] data = {0, 14, 2, 29, 1, 1, 0, 0};
        prs.parseMsg(data);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMsgTooShortForAllPeerFields() {
        byte[] data = {0, 12, 2, 29, 1, 1, 0, 0, 127, 0, 0, 1};
        prs.parseMsg(data);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidHostKeyPeerMsg() {
        byte[] data = {0, 13, 2, 29, 1, 1, 0, 0, 127, 0, 0, 1, 1};
        prs.parseMsg(data);
    }
}
