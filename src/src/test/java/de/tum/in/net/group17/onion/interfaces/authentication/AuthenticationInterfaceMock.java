package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs1ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs2ParsedMessage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Marko Dorfhuber(PraMiD) on 01.08.17.
 */
public class AuthenticationInterfaceMock implements AuthenticationInterface {
    private static String TEST_PAYLOAD = "Diffie-Hellman";

    public Map<Short, Peer> sessions;

    public AuthenticationInterfaceMock() {
        sessions = new HashMap<>();
    }

    @Override
    public void startSession(Peer peer, RequestResult callback) throws ParsingException {
        try {
            short sessionId = (short) getRndInt();
            sessions.put(sessionId, peer);
            Constructor<AuthSessionHs1ParsedMessage> c =
                    AuthSessionHs1ParsedMessage.class.getDeclaredConstructor(
                            short.class, int.class, byte[].class
                    );
            c.setAccessible(true);
            callback.respond(c.newInstance(sessionId, getRndInt(), TEST_PAYLOAD.getBytes()));
        } catch(Exception e) {
            assertTrue("Unable to create AUTH SESSION HS1 message! " + e.getMessage(), false);
        }
    }

    @Override
    public void forwardIncomingHandshake1(Peer peer, ParsedMessage hs1, RequestResult callback) {
        assertEquals("Unexpected message type!", MessageType.AUTH_SESSION_HS1, hs1.getType());

        /// TODO: May return Auth Error in this case
        assertArrayEquals("Invalid payload in incoming HS1 message!",
                TEST_PAYLOAD.getBytes(),
                ((AuthSessionHs1ParsedMessage)hs1).getPayload());

        try {
            short sessionId = (short) getRndInt();
            sessions.put(sessionId, peer);
            Constructor<AuthSessionHs2ParsedMessage> c =
                    AuthSessionHs2ParsedMessage.class.getDeclaredConstructor(
                            short.class, int.class, byte[].class
                    );
            c.setAccessible(true);
            callback.respond(c.newInstance(sessionId, getRndInt(), TEST_PAYLOAD.getBytes()));
        } catch(Exception e) {
            assertTrue("Unable to create AUTH SESSION HS2 message! " + e.getMessage(), false);
        }
    }

    @Override
    public void forwardIncomingHandshake2(Peer peer, short sessionId, byte[] payload) throws ParsingException {
        assertTrue("Unknown session ID!", sessions.containsKey(sessionId));

        /// TODO: May return Auth Error in this case
        assertArrayEquals("Invalid payload in incoming HS2 message!",
                TEST_PAYLOAD.getBytes(),
                payload);
    }

    @Override
    public void encrypt(Tunnel tunnel, RequestResult callback) {
        throw new NotImplementedException();
    }

    @Override
    public void decrypt(Tunnel tunnel, RequestResult callback) {
        throw new NotImplementedException();
    }

    private int getRndInt() {
        return (new Random()).nextInt();
    }
}
