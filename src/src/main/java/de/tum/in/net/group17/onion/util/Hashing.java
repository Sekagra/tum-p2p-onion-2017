package de.tum.in.net.group17.onion.util;

import org.bouncycastle.util.encoders.Hex;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides static hashing functions for all other classes to use.
 * Created by Christoph Rudolf on 27.05.17.
 */
public class Hashing {
    public static String Sha256(byte[] data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(data);
        return new String(Hex.encode(hash));
    }
}
