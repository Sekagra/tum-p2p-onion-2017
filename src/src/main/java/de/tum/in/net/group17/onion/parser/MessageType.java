package de.tum.in.net.group17.onion.parser;

/**
 * All message types specified in the current Voidphone specification.
 * Used by the concrete Parser implementations for the current Voidphone.
 * Sadly, Java doesn't provide C#-like enums that can be used directly as
 * a numeric value, hence the getValue()-method...
 *
 * Created by Christoph Rudolf on 25.05.17.
 */
public enum MessageType {
    GOSSIP_ANNOUNCE((short)500),
    GOSSIP_NOTIFY((short)501),
    GOSSIP_NOTIFICATION((short)502),
    GOSSIP_VALIDATION((short)503),

    NSE_QUERY((short)520),
    NSE_ESTIMATE((short)521),

    RSE_QUERY((short)540),
    RSE_PEER((short)541),

    ONION_TUNNEL_BUILD((short)560),
    ONION_TUNNEL_READY((short)561),
    ONION_TUNNEL_INCOMING((short)562),
    ONION_TUNNEL_DESTROY((short)563),
    ONION_TUNNEL_DATA((short)564),
    ONION_ERROR((short)565),
    ONION_COVER((short)566),

    AUTH_SESSION_START((short)600),
    AUTH_SESSION_HS1((short)601),
    AUTH_SESSION_INCOMING_HS1((short)602),
    AUTH_SESSION_HS2((short)603),
    AUTH_SESSION_INCOMING_HS2((short)604),
    AUTH_LAYER_ENCRYPT((short)605),
    AUTH_LAYER_ENCRYPT_RESP((short)606),
    AUTH_LAYER_DECRYPT((short)607),
    AUTH_LAYER_DECRYPT_RESP((short)608),
    AUTH_SESSION_CLOSE((short)609);

    private final short value;
    MessageType(short value) {
        this.value = value;
    }

    public short getValue() {
        return this.value;
    }
}
