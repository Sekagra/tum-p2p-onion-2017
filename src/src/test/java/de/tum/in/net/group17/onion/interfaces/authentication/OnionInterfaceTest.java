package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.interfaces.onion.OnionInterfaceImpl;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionToOnionParserImpl;
import org.junit.Test;

/**
 * Created by Christoph Rudolf on 21.06.17.
 */
public class OnionInterfaceTest {
    @Test
    public void receiveRawData() throws Exception {
        OnionInterfaceImpl intf = new OnionInterfaceImpl(new ConfigurationProvider(), new OnionToOnionParserImpl());
        System.out.println("Server is listening for test...");
        intf.listen();
    }
}
