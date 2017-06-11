package de.tum.in.net.group17.onion.interfaces.authentication;

import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.model.results.RequestResult;
import de.tum.in.net.group17.onion.model.Peer;
import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParserImpl;
import org.junit.Test;

/**
 * Created by Christoph Rudolf on 27.05.17.
 */
public class AuthenticationInterfaceImplTest {
    @Test
    public void startSession() throws Exception {
        AuthenticationInterfaceImpl intf = new AuthenticationInterfaceImpl(new ConfigurationProvider(), new AuthenticationParserImpl());

        Peer peer = new Peer(new byte[] {45,45,45,45});
        intf.startSession(peer, new RequestResult() {
            public void respond(ParsedMessage result) {
                System.out.println("Received response");
            }
        });
    }

}