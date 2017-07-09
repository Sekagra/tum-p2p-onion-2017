package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.config.ConfigurationProviderImpl;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterfaceImpl;
import de.tum.in.net.group17.onion.parser.onionapi.OnionApiParserImpl;
import org.junit.Test;

/**
 * Test for various aspects of the OnionApiInterface and its implementations.
 * Created by Christoph Rudolf on 11.06.17.
 */
public class OnionApiInterfaceTest {
    @Test
    public void receiveRawData() throws Exception {
        System.out.println("Server is listening for test...");
        OnionApiInterfaceImpl intf = new OnionApiInterfaceImpl(new ConfigurationProviderImpl(), new OnionApiParserImpl());
        intf.listen();
    }
}
