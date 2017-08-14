package de.tum.in.net.group17.onion.parser.authentication;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Christoph Rudolf on 25.05.17.
 */
public class AuthenticationParserImplTest {
    private static AuthenticationParser parser;

    @BeforeClass
    public static void setUp() throws Exception {
        parser = new AuthenticationParserImpl();
    }

    @Test
    public void buildSessionClose() throws Exception {
        parser.buildSessionClose((short)15);
    }

}