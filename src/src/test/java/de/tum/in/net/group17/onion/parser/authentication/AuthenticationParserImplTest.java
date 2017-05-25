package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.ParsedMessage;
import de.tum.in.net.group17.onion.parser.VoidphoneParser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class AuthenticationParserImplTest {
    private AuthenticationParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new AuthenticationParserImpl();
    }

    @Test
    public void buildSessionClose() throws Exception {
        parser.buildSessionClose((short)15);
    }

}