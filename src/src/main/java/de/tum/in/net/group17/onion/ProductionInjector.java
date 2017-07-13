package de.tum.in.net.group17.onion;

import com.google.inject.AbstractModule;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.config.ConfigurationProviderImpl;
import de.tum.in.net.group17.onion.interfaces.authentication.AuthenticationInterface;
import de.tum.in.net.group17.onion.interfaces.authentication.AuthenticationInterfaceImpl;
import de.tum.in.net.group17.onion.interfaces.onion.OnionInterface;
import de.tum.in.net.group17.onion.interfaces.onion.OnionInterfaceImpl;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterfaceImpl;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingInterface;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingInterfaceImpl;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParser;
import de.tum.in.net.group17.onion.parser.authentication.AuthenticationParserImpl;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionToOnionParser;
import de.tum.in.net.group17.onion.parser.onion2onion.OnionToOnionParserImpl;
import de.tum.in.net.group17.onion.parser.onionapi.OnionApiParser;
import de.tum.in.net.group17.onion.parser.onionapi.OnionApiParserImpl;
import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParser;
import de.tum.in.net.group17.onion.parser.rps.RandomPeerSamplingParserImpl;

/**
 * Injector for production usage of the Onion module, thus, not using mocks as a testing injector would.
 * Created by Christoph Rudolf on 09.07.17.
 */
public class ProductionInjector extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConfigurationProvider.class).to(ConfigurationProviderImpl.class);

        // bind interfaces to other modules to their implementations
        bind(AuthenticationInterface.class).to(AuthenticationInterfaceImpl.class);
        bind(OnionInterface.class).to(OnionInterfaceImpl.class);
        bind(OnionApiInterface.class).to(OnionApiInterfaceImpl.class);
        bind(RandomPeerSamplingInterface.class).to(RandomPeerSamplingInterfaceImpl.class);

        // bind interfaces of parsers to the parser implementation
        bind(AuthenticationParser.class).to(AuthenticationParserImpl.class);
        bind(OnionToOnionParser.class).to(OnionToOnionParserImpl.class);
        bind(OnionApiParser.class).to(OnionApiParserImpl.class);
        bind(RandomPeerSamplingParser.class).to(RandomPeerSamplingParserImpl.class);
    }
}