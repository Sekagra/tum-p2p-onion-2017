package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.model.LidImpl;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.model.Tunnel;
import de.tum.in.net.group17.onion.model.TunnelSegment;
import de.tum.in.net.group17.onion.parser.ParsingException;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs1ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthSessionHs2ParsedMessage;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionTunnelTransportParsedMessage;
import de.tum.in.net.group17.onion.util.LidFingerprinting;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Arrays;

/**
 * Created by Marko Dorfhuber(PraMiD) on 01.08.17.
 */
public class AuthenticationInterfaceMock implements AuthenticationInterface {
    private static String TEST_PAYLOAD = "Diffie-Hellman";

    public List<Short> sessions;

    /**
     * Create a new AuthenticationInterfaceMock with an empty session list.
     */
    public AuthenticationInterfaceMock() {
        sessions = new ArrayList<>();
    }

    /**
     * @inheritDoc
     */
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

    /**
     * @inheritDoc
     */
    @Override
    public void closeSession(short sessionId) throws ParsingException {
        sessions.remove(new Short(sessionId));
    }

    /**
     * @inheritDoc
     */
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

    /**
     * @inheritDoc
     */
    @Override
    public void forwardIncomingHandshake2(short sessionId, byte[] payload) throws ParsingException {
        if(!sessions.contains(sessionId))
            throw new RuntimeException("Unknown session ID!");

        // TODO: May return Auth Error in this case
        if(!Arrays.equals(TEST_PAYLOAD.getBytes(), payload))
            throw new RuntimeException("Invalid payload in incoming HS2 message!");
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, List<TunnelSegment> segments) throws InterruptedException, ParsingException {
        // Set the new arrays to handle the case if we only get a copy of the stored array
        // We encode by adding the number of encryptions in front of the data
        byte[] payload = message.getData();
        byte[] newData = new byte[payload.length + 2 * LidImpl.LENGTH];
        System.arraycopy(payload, 0, newData, 2 * LidImpl.LENGTH, payload.length);
        if(segments.size() == 1) {
            System.arraycopy(segments.get(0).getLid().serialize(), 0, newData, LidImpl.LENGTH, LidImpl.LENGTH);
        } else if(segments.size() == 2) {
                System.arraycopy(segments.get(0).getLid().serialize(), 0, newData, 0, LidImpl.LENGTH);
                System.arraycopy(segments.get(1).getLid().serialize(), 0, newData, LidImpl.LENGTH, LidImpl.LENGTH);
        } else {
            throw new RuntimeException("The mock can only deal with exactly two tunnel segments.");
        }

        message.setData(newData);

        return message;
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage encrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment, boolean isCipher) throws InterruptedException, ParsingException, AuthException {
        // Set the new arrays to handle the case if we only get a copy of the stored array
        // We encode by adding the number of encryptions in front of the data
        byte[] payload = message.getData();

        if(message.forMe()) { // MAGIC in plain
            byte[] newData = new byte[payload.length + 2 * LidImpl.LENGTH];
            System.arraycopy(payload, 0, newData, 2 * LidImpl.LENGTH, payload.length);
            System.arraycopy(segment.getLid().serialize(), 0, newData, LidImpl.LENGTH, LidImpl.LENGTH);
            message.setData(newData);
        } else {
            System.arraycopy(segment.getLid().serialize(), 0, payload, 0, LidImpl.LENGTH);
            message.setData(payload);
        }

        return message;
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, List<TunnelSegment> segments) throws InterruptedException, ParsingException {
        // Set the new arrays to handle the case if we only get a copy of the stored array
        byte[] payload = message.getData();

        if(segments.size() <= 2) {
            // expect first segment at pos 0, and second segment at pos LidImpl.LENGTH
            byte[] lid1 = Arrays.copyOfRange(message.getData(), 0, LidImpl.LENGTH);
            byte[] lid2 = Arrays.copyOfRange(message.getData(), LidImpl.LENGTH, 2*LidImpl.LENGTH);
            if(getLidFingerprint(lid1) == 0 || Arrays.equals(lid1, segments.get(0).getLid().serialize())) {
                try {
                    if (Arrays.equals(lid2, segments.get((getLidFingerprint(lid1) == 0) ? 0 : 1).getLid().serialize())) {
                        message.setData(Arrays.copyOfRange(message.getData(), 2 * LidImpl.LENGTH, message.getData().length));
                        return message;
                    }
                } catch(IndexOutOfBoundsException e) {
                    throw e;
                }
            }

            if(segments.size() < 2) {
                throw new RuntimeException("Wrong encryption, found "
                        + getLidFingerprint(lid1)
                        + " and expected "
                        + getLidFingerprint(segments.get(0).getLid().serialize()));
            } else {
                throw new RuntimeException("Wrong encryption, found "
                        + getLidFingerprint(lid1) + ", " + getLidFingerprint(lid2)
                        + " and expected "
                        + getLidFingerprint(segments.get(0).getLid().serialize()) + ", " + getLidFingerprint(segments.get(1).getLid().serialize()));
            }
        } else {
            throw new RuntimeException("The mock can only deal with exactly two tunnel segments.");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public OnionTunnelTransportParsedMessage decrypt(OnionTunnelTransportParsedMessage message, TunnelSegment segment) throws InterruptedException, ParsingException {
        byte[] lid1 = Arrays.copyOfRange(message.getData(), 0, LidImpl.LENGTH);
        byte[] lid2 = Arrays.copyOfRange(message.getData(), LidImpl.LENGTH, 2*LidImpl.LENGTH);

        // check if first Lid Block is empty and compare to second one, remove it
        if(getLidFingerprint(lid1) == 0) {
            if(Arrays.equals(lid2, segment.getLid().serialize())) {
                message.setData(Arrays.copyOfRange(message.getData(), 2 * LidImpl.LENGTH, message.getData().length));
            } else {
                throw new RuntimeException("Wrong encryption, found "
                        + getLidFingerprint(lid2)
                        + " and expected "
                        + getLidFingerprint(segment.getLid().serialize()));
            }
        } else {
            // otherwise compare to first and take it away
            byte[] payload = message.getData();
            if(Arrays.equals(lid1, segment.getLid().serialize())) {
                for(int i=0; i < 16; i++) {
                    payload[i] = 0x0;
                }
                message.setData(payload);
            } else {
                throw new RuntimeException("Wrong encryption, found "
                        + getLidFingerprint(lid1)
                        + " and expected "
                        + getLidFingerprint(segment.getLid().serialize()));
            }
        }
        return message;
    }

    /**
     * Get a new random integer.
     *
     * @return A random integer.
     */
    private int getRndInt() {
        return (new Random()).nextInt();
    }

    /**
     * Return the fingerprint of a LID encoded as byte array.
     *
     *
     * @param rawLid byte[] containing the LID.
     *
     * @return The fingerprint of the LID.
     */
    private int getLidFingerprint(byte[] rawLid) {
        return LidFingerprinting.fingerprint(rawLid);
    }
}
