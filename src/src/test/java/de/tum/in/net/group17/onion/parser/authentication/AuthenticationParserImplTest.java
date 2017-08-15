package de.tum.in.net.group17.onion.parser.authentication;

import de.tum.in.net.group17.onion.parser.ParsingException;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class AuthenticationParserImplTest {
    private static AuthenticationParser parser;

    /**
     * Set-up test environment: Create the authentication parser to use.
     */
    @BeforeClass
    public static void setUp() {
        parser = new AuthenticationParserImpl();
    }

    /**
     * Test creation of an AUTH SESSION CLOSE message.
     *
     * @throws ParsingException If the parser could not build the message.
     */
    @Test
    public void buildSessionClose() throws ParsingException {
        parser.buildSessionClose((short)15);
    }

}