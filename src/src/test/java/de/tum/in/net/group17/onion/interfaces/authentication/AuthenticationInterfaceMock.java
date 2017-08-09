package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.parser.MessageType;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs1ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs2ParsedMessage;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionTunnelTransportParsedMessage;
import org.bouncycastle.util.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Marko Dorfhuber(PraMiD) on 01.08.17.
 */
public class AuthenticationInterfaceMock implements AuthenticationInterface {
    private static String TEST_PAYLOAD = "Diffie-Hellman";

    public List<Short> sessions;

    public AuthenticationInterfaceMock() {
        sessions = new ArrayList<>();
    }

    @Override
    public AuthSessionHs1ParsedMessage startSession(Peer peer) throws ParsingException, InterruptedException {
        try {
            short sessionId = (short) getRndInt();
            sessions.add(sessionId);
            Constructor<AuthSessionHs1ParsedMessage> c =
                    AuthSessionHs1ParsedMessage.class.getDeclaredConstructor(
                            short.class, int.class, byte[].class
                    );
            c.setAccessible(true);
            return c.newInstance(sessionId, getRndInt(), TEST_PAYLOAD.getBytes());
        } catch(Exception e) {
            throw new RuntimeException("Unable to create AUTH SESSION HS1 message! " + e.getMessage());
        }
    }

    @Override
    public AuthSessionHs2ParsedMessage forwardIncomingHandshake1(byte[] payload) throws ParsingException, InterruptedException {
        // TODO: May return Auth Error in this case
        if(!Arrays.equals(TEST_PAYLOAD.getBytes(),  payload))
            throw new RuntimeException("Invalid payload in incoming HS1 message!");

        try {
            short sessionId = (short) getRndInt();
            sessions.add(sessionId);
            Constructor<AuthSessionHs2ParsedMessage> c =
                    AuthSessionHs2ParsedMessage.class.getDeclaredConstructor(
                            short.class, int.class, byte[].class
                    );
            c.setAccessible(true);
            return c.newInstance(sessionId, getRndInt(), TEST_PAYLOAD.getBytes());
        } catch(Exception e) {
            throw new RuntimeException("Unable to create AUTH SESSION HS2 message! " + e.getMessage());
        }
    }


    @Override
    public void forwardIncomingHandshake2(short sessionId, byte[] payload) throws ParsingException {
        if(!sessions.contains(sessionId))
            throw new RuntimeException("Unknown session ID!");

        // TODO: May return Auth Error in this case
        if(!Arrays.equals(TEST_PAYLOAD.getBytes(), payload))
            throw new RuntimeException("Invalid payload in incoming HS2 message!");
    }

    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, Tunnel tunnel) throws InterruptedException, ParsingException {
        // Set the new arrays to handle the case if we only get a copy of the stored array
        // We encode by adding the number of encryptions in front of the data
        byte[] payload = message.getData();
        byte[] newData = new byte[payload.length + 1];
        System.arraycopy(payload, 0, newData, 1, payload.length);
        newData[0] = (byte)tunnel.getSegments().size();
        message.setData(newData);

        return message;
    }

    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException {
        // Set the new arrays to handle the case if we only get a copy of the stored array
        // We encode by adding the number of encryptions in front of the data
        byte[] payload = message.getData();

        if(message.forMe()) { // MAGIC in plain
            byte[] newData = new byte[payload.length + 1];
            System.arraycopy(payload, 0, newData, 1, payload.length);
            newData[0] = 1;
            message.setData(newData);
        } else {
            payload[0] += 1;
            message.setData(payload);
        }

        return message;
    }

    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, Tunnel tunnel) throws InterruptedException, ParsingException {
        // Set the new arrays to handle the case if we only get a copy of the stored array
        byte[] payload = message.getData();

        if(payload[0] != (byte)tunnel.getSegments().size())
            throw new RuntimeException("Number of encryptions does not match number of hops in the tunnel!" +
                    "Expected: " + tunnel.getSegments().size() + ". Got: " + (int)payload[0]);
        message.setData(Arrays.copyOfRange(payload, 1, payload.length));

        return message;
    }

    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException {
        // Set the new arrays to handle the case if we only get a copy of the stored array
        byte[] payload = message.getData();

        if(((int)payload[0]) < 1)
            throw new RuntimeException("Cannot decrypt. Too small number of previous encryptions!" +
                    " Got: " + (int)payload[0]);
        if((int)payload[0] == 1) { // Last encryption
            message.setData(Arrays.copyOfRange(payload, 1, payload.length));
        } else {
            payload[0] -= 1;
            message.setData(payload);
        }
        return message;
    }

    private int getRndInt() {
        return (new Random()).nextInt();
    }
}
