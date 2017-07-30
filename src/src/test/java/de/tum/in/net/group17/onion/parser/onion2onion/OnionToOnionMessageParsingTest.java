package de.tum.in.net.group17.onion.parser.onion2onion;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.tum.in.net.group17.onion.ParserUnitTestInjector;
import de.tum.in.net.group17.onion.model.Lid;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import org.bouncycastle.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

/**
 * Created by Marko Dorfhuber(PraMiD) on 26.07.17.
 *
 * This tests cover parsing and serialization of all ONION2ONION messages.
 */
public class OnionToOnionMessageParsingTest {
    private static Injector injector = Guice.createInjector(new ParserUnitTestInjector());
    private static OnionToOnionParser prs;
    private static Lid lid;

    @BeforeClass
    public static void initParser()
    {
        prs = injector.getInstance(OnionToOnionParser.class);
    }

    @BeforeClass
    public static void initLid()
    {
        lid = injector.getInstance(Lid.class);
    }

    /**
     * This tests targets the parsing of ONION TUNNEL INIT messages.
     */
    @Test
    public void testUnionTunnelInitParsing() throws ParsingException
    {
        byte[] header = {
                0x00, 0x18, 0x02, 0x53, // Header
        };
        byte[] payload = {
                0x02, 0x03, 0x04, 0x05  // Payload
        };

        ParsedMessage m = prs.parseMsg(Arrays.concatenate(header, lid.serialize(), payload));
        assertTrue("Wrong message type for ONION TUNNEL INIT message parsing!",
                m.getClass() == OnionTunnelInitParsedMessage.class);

        OnionTunnelInitParsedMessage msg = (OnionTunnelInitParsedMessage)m;
        assertTrue("Wrong message size of ONION TUNNEL INIT message!", msg.getSize() == 0x18);
        assertTrue("Wrong message type for ONION TUNNEL INIT message!",
                msg.getType() == MessageType.ONION_TUNNEL_INIT);
        assertEquals("Wrong LID in ONION TUNNEL INIT message!", lid, msg.getLid());
        assertArrayEquals("Wrong payload in ONION TUNNEL INIT message!", payload, msg.getAuthPayload());
    }

    @Test
    public void testOnionTunnelInitSerialization() throws ParsingException
    {
        byte[] header = {
                0x00, 0x18, 0x02, 0x53, // Header
        };
        byte[] payload = {
                0x02, 0x03, 0x04, 0x05  // Payload
        };

        byte[] testData = (new OnionTunnelInitParsedMessage(lid, payload)).serialize();

        assertArrayEquals("Faulty serialization of ONION TUNNEL INIT messages!",
                Arrays.concatenate(header, lid.serialize(), payload), testData);
    }

    @Test
    public void testOnionTunnelAcceptParsing() throws ParsingException
    {
        byte[] header = {
                0x00, 0x18, 0x02, 0x54, // Header
        };
        byte[] payload = {
                0x02, 0x03, 0x04, 0x05  // Payload
        };

        ParsedMessage m = prs.parseMsg(Arrays.concatenate(header, lid.serialize(), payload));
        assertTrue("Wrong message type for ONION TUNNEL ACCEPT message parsing!",
                m.getClass() == OnionTunnelAcceptParsedMessage.class);

        OnionTunnelAcceptParsedMessage msg = (OnionTunnelAcceptParsedMessage)m;
        assertTrue("Wrong message size of ONION TUNNEL ACCEPT message!", msg.getSize() == 0x18);
        assertTrue("Wrong message type for ONION TUNNEL ACCEPT message!",
                msg.getType() == MessageType.ONION_TUNNEL_ACCEPT);
        assertEquals("Wrong LID in ONION TUNNEL ACCEPT message!", lid, msg.getLid());
        assertArrayEquals("Wrong payload in ONION TUNNEL ACCEPT message!", payload, msg.getAuthPayload());
    }

    @Test
    public void testOnionTunnelAcceptSerialization() throws ParsingException
    {
        byte[] header = {
                0x00, 0x18, 0x02, 0x54, // Header
        };
        byte[] payload = {
                0x02, 0x03, 0x04, 0x05  // Payload
        };

        byte[] testData = (new OnionTunnelAcceptParsedMessage(lid, payload)).serialize();

        assertArrayEquals("Faulty serialization of ONION TUNNEL ACCEPT messages!",
                Arrays.concatenate(header, lid.serialize(), payload), testData);
    }

    @Test
    public void testOnionTunnelRelayParsingIpv4() throws ParsingException, UnknownHostException
    {
        byte[] header = {
                0x00, 0x30, 0x02, 0x55
        };

        byte[] portFlags = {
            0x00, 0x50, 0x00, 0x00
        };

        byte[] address = {
                127, 0, 0, 1
        };

        byte[] payload = {
                0x01, 0x02, 0x03, 0x04
        };

        // Just reuse the LID..
        ParsedMessage m = prs.parseMsg(Arrays.concatenate(new byte[][]{header,
                lid.serialize(),
                portFlags,
                address,
                lid.serialize(),
                payload
        }));

        assertEquals("Wrong parsed message type for ONION TUNNEL RELAY message!",
                OnionTunnelRelayParsedMessage.class, m.getClass());

        OnionTunnelRelayParsedMessage msg = (OnionTunnelRelayParsedMessage)m;
        assertEquals("Wrong message size for ONION TUNNEL RELAY message!", 0x30,
                msg.getSize());
        assertEquals("Wrong message type for ONION TUNNEL RELAY message!", MessageType.ONION_TUNNEL_RELAY,
                msg.getType());
        assertEquals("Wrong port in parsed ONION TUNNEL RELAY message!", 0x50, msg.getPort());
        assertTrue("IPv4 flag not set in IPv4 ONION TUNNEL RELAY message!", msg.isIpv4());
        assertEquals("Wrong IP address in ONION TUNNEL RELAY message!", Inet4Address.getByName("127.0.0.1"),
                msg.getAddress());
        assertEquals("Wrong incoming LID in ONION TUNNEL RELAY message!", lid, msg.getLid());
        assertEquals("Wrong LID of outgoing tunnel in ONION TUNNEL RELAY message!", lid,
                msg.getOutgoingTunnel());
        assertArrayEquals("Wrong payload in ONION TUNNEL RELAY message!", payload, msg.getPayload());
    }

    @Test
    public void testOnionTunnelRelayParsingIpv6() throws ParsingException, UnknownHostException
    {
        byte[] header = {
                0x00, 0x3C, 0x02, 0x55
        };

        byte[] portFlags = {
                0x00, 0x50, (byte)0x80, 0x00
        };

        byte[] address = {
                0x01, 0x02, 0x03, 0x04,
                0x05, 0x06, 0x07, 0x08,
                0x09, 0x10, 0x11, 0x12,
                0x13, 0x14, 0x15, 0x16,
        };

        byte[] payload = {
                0x01, 0x02, 0x03, 0x04
        };

        // Just reuse the LID..
        ParsedMessage m = prs.parseMsg(Arrays.concatenate(new byte[][]{header,
                lid.serialize(),
                portFlags,
                address,
                lid.serialize(),
                payload
        }));

        assertEquals("Wrong parsed message type for ONION TUNNEL RELAY message!",
                OnionTunnelRelayParsedMessage.class, m.getClass());

        OnionTunnelRelayParsedMessage msg = (OnionTunnelRelayParsedMessage)m;
        assertEquals("Wrong message size for ONION TUNNEL RELAY message!", 0x3C,
                msg.getSize());
        assertEquals("Wrong message type for ONION TUNNEL RELAY message!", MessageType.ONION_TUNNEL_RELAY,
                msg.getType());
        assertEquals("Wrong port in parsed ONION TUNNEL RELAY message!", 0x50, msg.getPort());
        assertFalse("IPv4 flag set in IPv6 ONION TUNNEL RELAY message!", msg.isIpv4());
        assertEquals("Wrong IP address in ONION TUNNEL RELAY message!", Inet6Address.getByAddress(address),
                msg.getAddress());
        assertEquals("Wrong incoming LID in ONION TUNNEL RELAY message!", lid, msg.getLid());
        assertEquals("Wrong LID of outgoing tunnel in ONION TUNNEL RELAY message!", lid,
                msg.getOutgoingTunnel());
        assertArrayEquals("Wrong payload in ONION TUNNEL RELAY message!", payload, msg.getPayload());
    }

    @Test
    public void testOnionTunnelRelaySerializationIpv4() throws UnknownHostException
    {
        byte[] header = {
                0x00, 0x30, 0x02, 0x55
        };

        byte[] portFlags = {
                0x00, 0x50, 0x00, 0x00
        };

        byte[] address = {
                127, 0, 0, 1
        };

        byte[] payload = {
                0x01, 0x02, 0x03, 0x04
        };

        byte[] testData = new OnionTunnelRelayParsedMessage(lid,
                lid,
                Inet4Address.getByName("127.0.0.1"),
                (short)80,
                payload).serialize();

        assertArrayEquals("Faulty serialization of ONION TUNNEL RELAY message (IPv4)!",
                Arrays.concatenate(new byte[][]{
                    header, lid.serialize(), portFlags, address, lid.serialize(), payload
                }), testData);
    }

    @Test
    public void testOnionTunnelRelaySerializationIpv6() throws UnknownHostException
    {
        byte[] header = {
                0x00, 0x3C, 0x02, 0x55
        };

        byte[] portFlags = {
                0x00, 0x50, (byte)0x80, 0x00
        };

        byte[] address = {
                0x01, 0x02, 0x03, 0x04,
                0x05, 0x06, 0x07, 0x08,
                0x09, 0x10, 0x11, 0x12,
                0x13, 0x14, 0x15, 0x16,
        };

        byte[] payload = {
                0x01, 0x02, 0x03, 0x04
        };

        byte[] testData = new OnionTunnelRelayParsedMessage(lid,
                lid,
                Inet6Address.getByAddress(address),
                (short)80,
                payload).serialize();

        assertArrayEquals("Faulty serialization of ONION TUNNEL RELAY message (IPv6)!",
                Arrays.concatenate(new byte[][]{
                        header, lid.serialize(), portFlags, address, lid.serialize(), payload
                }), testData);
    }

    @Test
    public void testOnionTunnelVoiceParsing() throws ParsingException
    {
        byte[] header = {
                0x00, 0x08, 0x02, 0x52
        };

        byte[] data = {
                0x01, 0x02, 0x03, 0x04
        };

        ParsedMessage m = prs.parseMsg(Arrays.concatenate(header, data));
        assertEquals("Wrong parsed message type for ONION TUNNEL VOICE message!",
                OnionTunnelVoiceParsedMessage.class, m.getClass());

        OnionTunnelVoiceParsedMessage msg = (OnionTunnelVoiceParsedMessage)m;
        assertEquals("Wrong size for ONION TUNNEL VOICE message!", 8, msg.getSize());
        assertEquals("Wrong message type for ONION TUNNEL VOICE message!",
                msg.getType(),
                MessageType.ONION_TUNNEL_VOICE);
        assertArrayEquals("Invalid data for ONION TUNNEL VOICE message!", data, msg.getData());
    }

    @Test
    public void testOnionTunnelVoiceSerialization() throws ParsingException
    {
        byte[] header = {
                0x00, 0x08, 0x02, 0x52
        };

        byte[] data = {
                0x01, 0x02, 0x03, 0x04
        };

        byte[] testData = prs.buildOnionTunnelVoiceMsg(data).serialize();

        assertArrayEquals("Invalid serialization of ONION TUNNEL VOICE message!",
                Arrays.concatenate(header, data), testData);
    }

    @Test
    public void testOnionTunnelTeardownParsing() throws ParsingException
    {
        byte[] header = {
            0x00, 0x14, 0x02, 0x57
        };

        ParsedMessage m = prs.parseMsg(Arrays.concatenate(header, lid.serialize()));

        assertEquals("Wrong parsed message type for ONION TUNNEL TEARDOWN message!",
                OnionTunnelTeardownParsedMessage.class, m.getClass());

        OnionTunnelTeardownParsedMessage msg = (OnionTunnelTeardownParsedMessage)m;
        assertEquals("Wrong message size for ONION TUNNEL TEARDOWN message!", 4 + lid.getSize(),
                msg.getSize());
        assertEquals("Wrong message type for ONION TUNNEL TEARDOWN message!", MessageType.ONION_TUNNEL_TEARDOWN,
                msg.getType());
        assertEquals("Wrong LID in ONION TUNNEL TEARDOWN message!", lid,
                msg.getLid());
    }

    @Test
    public void testOnionTunnelTeardownSerialization() throws ParsingException {
        byte[] header = {
                0x00, 0x14, 0x02, 0x57
        };

        byte[] testData = prs.buildOnionTunnelTeardownMsg(lid.serialize()).serialize();

        assertArrayEquals("Invalid serialization of ONION TUNNEL TEARDOWN messages!",
                Arrays.concatenate(header, lid.serialize()),
                testData);

    }
}