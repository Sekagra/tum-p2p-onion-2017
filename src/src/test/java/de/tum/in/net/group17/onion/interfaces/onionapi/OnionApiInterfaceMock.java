package de.tum.in.net.group17.onion.interfaces.onionapi;

import com.google.inject.Inject;
import de.tum.in.net.group17.onion.parser.onionapi.*;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.util.Arrays;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.assertTrue;

/**
 * Created by Marko Dorfhuber(PraMiD) on 31.07.17.
 */
public class OnionApiInterfaceMock implements OnionApiInterface {
    private static final int INTERVAL_MS = 1000;
    private static final String VOICE_DATA = "VOICE";

    private int outgoingTunnelId = -1; // Negative ID means that no tunnel is currently established
    private boolean tunnelEstPending = false;
    private boolean receivedCover, sentCover, receivedVoice, sentVoice;
    private List<Integer> incomingTunnelIds = new ArrayList<>();
    private OnionApiCallback callbacks;

    private Timer testRunner;
    @Inject
    private OnionApiParser parser;
    // Only the sender of the test initiates voice and cover traffic transfer -> The receiver just sends back the received data
    private boolean sender;

    public OnionApiInterfaceMock() throws IOException {
        ASN1Primitive endpointKey = getEndpointKey();
        InetAddress endpointIp = Inet4Address.getByName("localhost");
        short port = 8890;
        this.sender = false;

        TimerTask tsk = new TimerTask() {
            // Reflection.. The price we pay for protecting the ParsedMessage constructors..
            @Override
            public void run() {
                if(tunnelEstPending) { // Just wait for tunnel establishment
                    return;
                } else if(outgoingTunnelId == -1) {
                    try {
                        Constructor<OnionTunnelBuildParsedMessage> c =
                                OnionTunnelBuildParsedMessage.class.getDeclaredConstructor(
                                        ASN1Primitive.class, InetAddress.class, short.class
                                );
                        c.setAccessible(true);
                        OnionTunnelBuildParsedMessage msg = c.newInstance(endpointKey, endpointIp, port);
                        callbacks.receivedTunnelBuild(msg);

                        tunnelEstPending = true;
                    } catch (Exception e) {
                        assertTrue("Cannot create ONION TUNNEL BUILD message! " + e.getMessage(), false);
                    }
                } else if(!sentVoice && sender) {
                    try {
                        Constructor<OnionTunnelDataParsedMessage> c =
                                OnionTunnelDataParsedMessage.class.getDeclaredConstructor(
                                       int.class, byte[].class
                                );
                        c.setAccessible(true);
                        OnionTunnelDataParsedMessage msg = c.newInstance(outgoingTunnelId, VOICE_DATA.getBytes());
                        callbacks.receivedVoiceData(msg);
                        sentVoice = true;
                    } catch (Exception e) {
                        assertTrue("Cannot create ONION TUNNEL VOICE message! " + e.getMessage(), false);
                    }
                } else if(!sentCover && sender) {
                    try {
                        Constructor<OnionCoverParsedMessage> c =
                                OnionCoverParsedMessage.class.getDeclaredConstructor(
                                        short.class
                                );
                        c.setAccessible(true);
                        OnionCoverParsedMessage msg = c.newInstance(10);
                        callbacks.receivedCoverData(msg);
                        sentCover = true;
                    } catch (Exception e) {
                        assertTrue("Cannot create ONION COVER message! " + e.getMessage(), false);
                        return;
                    }
                } else if(receivedCover && receivedVoice) {
                    try {
                        Constructor<OnionTunnelDestroyParsedMessage> c =
                                OnionTunnelDestroyParsedMessage.class.getDeclaredConstructor(
                                        int.class
                                );
                        c.setAccessible(true);
                        OnionTunnelDestroyParsedMessage msg = c.newInstance(outgoingTunnelId);
                        callbacks.receivedDestroy(msg);
                    } catch (Exception e) {
                        assertTrue("Cannot create ONION TUNNEL DESTROY message! " + e.getMessage(), false);
                        return;
                    }
                }
            }
        };

        testRunner = new Timer();
        testRunner.schedule(tsk, 0, INTERVAL_MS);
    }

    @Override
    public void listen(OnionApiCallback callback) {
        this.callbacks = callback;
    }

    @Override
    public void sendIncoming(OnionTunnelIncomingParsedMessage msg) throws OnionApiException {
        incomingTunnelIds.add(msg.getTunnelId());
    }

    @Override
    public void sendReady(OnionTunnelReadyParsedMessage msg) throws OnionApiException {
        outgoingTunnelId = msg.getTunnelId(); // Our tunnel is ready for usage

        tunnelEstPending = false;
    }

    @Override
    public void sendError(OnionErrorParsedMessage msg) throws OnionApiException {
        throw new OnionApiException("ONION ERROR message!"); // This should not happen during testing
    }

    @Override
    public void sendVoiceData(OnionTunnelDataParsedMessage msg) throws OnionApiException {
        if(Arrays.areEqual(VOICE_DATA.getBytes(), msg.getData()) && !receivedVoice) {
            receivedVoice = true;

            if(!sender) {
                try {
                    Constructor<OnionTunnelDataParsedMessage> c =
                            OnionTunnelDataParsedMessage.class.getDeclaredConstructor(
                                    int.class, byte[].class
                            );
                    c.setAccessible(true);
                    OnionTunnelDataParsedMessage message = c.newInstance(outgoingTunnelId, VOICE_DATA.getBytes());
                    callbacks.receivedVoiceData(message);
                    sentVoice = true;
                } catch (Exception e) {
                    assertTrue("Cannot create ONION TUNNEL VOICE message! " + e.getMessage(), false);
                }
            }
        } else if(!receivedCover) {
            receivedCover = true;

            if(!sender) {
                try {
                    Constructor<OnionCoverParsedMessage> c =
                            OnionCoverParsedMessage.class.getDeclaredConstructor(
                                    short.class
                            );
                    c.setAccessible(true);
                    OnionCoverParsedMessage message = c.newInstance(10);
                    callbacks.receivedCoverData(message);
                    sentCover = true;
                } catch (Exception e) {
                    assertTrue("Cannot create ONION COVER message! " + e.getMessage(), false);
                    return;
                }
            }
        } else {
            assertTrue("Received invalid data!" +
                    "Either cover data was sent twice or the tunnel corrupted our data!", false);
            return;
        }
    }

    /**
     * Set if this instance of the CM mock is used as sender or receiver
     *
     * @param sender Is this instance the sender?
     */
    public void setSender(boolean sender)
    {
        this.sender = sender;
    }

    public ASN1Primitive getEndpointKey() throws IOException {
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

            byte[] key = new byte[tmp.length];
            // Java has signed bytes => To specify values > 127 we have to use this method
            // As this is only a RpsPeerParsedMessage case, performance is no issue
            for(int i = 0; i < tmp.length; ++i) {
                key[i] = (byte)tmp[i];
            }

            return (new ASN1InputStream(new ByteArrayInputStream(key)).readObject()).toASN1Primitive();
    }
}
