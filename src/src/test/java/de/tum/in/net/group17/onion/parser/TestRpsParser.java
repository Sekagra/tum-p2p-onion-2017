package de.tum.in.net.group17.onion.parser;

import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParserImpl;
import de.tum.in.net.group17.onion.parser.rps.RpsParsedObject;
import org.junit.Test;

/**
 * Created by Marko Dorfhuber(PraMiD) on 25.05.17.
 */
public class TestRpsParser {

    /**
     * Test if the RPS parser creates the right QUERY message.
     */
    @Test
    public void testRpsQueryMessage() {
        RpsParsedObject obj = (new RandomPeerSamplingParserImpl()).buildRpsQueryMsg();
        byte[] data = obj.getData();

        if(data[0] != 0 ||
                data[1] != 4 ||
                data[2] != 2 ||
                data[3] != 28 ||
                obj.getType() != RpsParsedObject.RPS_MSG_TYPE.RPS_QUERY) {
            assert(false);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidMessageType() {
        byte[] data = {0, 4, 1, 1};
        (new RandomPeerSamplingParserImpl()).parseMsg(data);
    }
}
