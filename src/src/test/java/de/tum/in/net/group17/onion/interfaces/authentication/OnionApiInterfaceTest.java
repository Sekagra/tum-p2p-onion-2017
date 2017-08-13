package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.config.ConfigurationProviderImpl;
import de.tum.in.net.group17.onion.config.ConfigurationProviderMock;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterfaceImpl;
import de.tum.in.net.group17.onion.parser.onionapi.OnionApiParserImpl;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for various aspects of the OnionApiInterface and its implementations.
 * Created by Christoph Rudolf on 11.06.17.
 */
public class OnionApiInterfaceTest {
    @Test
    @Ignore
    public void receiveRawData() throws Exception {
        System.out.println("Server is listening for test...");
        ConfigurationProvider config = new ConfigurationProviderMock(5000,
                6000,
                7000,
                9000,
                1,
                "localhost",
                "localhost",
                "localhost",
                "localhost",
                60);
        OnionApiInterfaceImpl intf = new OnionApiInterfaceImpl(config, new OnionApiParserImpl());
        //intf.listen();
    }
}
