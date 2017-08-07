package de.tum.in.net.group17.onion;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.tum.in.net.group17.onion.config.ConfigurationProvider;
import de.tum.in.net.group17.onion.config.ConfigurationProviderMock;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterfaceMock;
import de.tum.in.net.group17.onion.parser.onionapi.OnionApiParser;
import de.tum.in.net.group17.onion.parser.onionapi.OnionApiParserImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Marko Dorfhuber(PraMiD) on 01.08.17.
 */
public class OrchestratorTest {
    private static AbstractModule senderModule, receiverModule, ihModule;
    private static Injector senderInjector, receiverInjector, ihInjector;

    private static Orchestrator sender, ih, receiver;

    @BeforeClass
    public static void initPeers() throws Exception {
        createInjectors();

        sender = senderInjector.getInstance(Orchestrator.class);
        receiver = receiverInjector.getInstance(Orchestrator.class);
        ih = ihInjector.getInstance(Orchestrator.class);
    }


    @Test
    public void testOrchestrator() {
        Throwable[] exc = new Throwable[1];

        ih.start();
        receiver.start();
        sender.start();

        Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                exc[0] = e;
            }
        };

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Set<Thread> testThreads = new HashSet<>();
        for (Thread t : threadSet) {
            if(t.getName().startsWith("Timer-"))
                testThreads.add(t);
        }

        for(Thread t : testThreads)
            t.setUncaughtExceptionHandler(handler);

        for(Thread t : testThreads)
            try {
                t.join();
            } catch (InterruptedException e) {
                fail("Interrupted!");
            }

        if(exc[0] != null)
            fail(exc[0].getMessage());
    }

    public static void createInjectors() throws NoSuchFieldException, IllegalAccessException, UnknownHostException {
        senderModule = new AbstractModule() {
            @Override
            protected void configure() {
                ConfigurationProvider config = null;
                OnionApiInterfaceMock apiInterface = null;
                try {
                    config = new ConfigurationProviderMock(5000,
                            6000,
                            7000,
                            9000,
                            1,
                            "localhost",
                            "localhost",
                            "localhost",
                            "localhost");
                } catch (UnknownHostException e) {
                    assertTrue("Unable to create sender configuration! " + e.getMessage(), false);
                }
                try {
                    apiInterface = new OnionApiInterfaceMock();
                } catch (IOException e) {
                    assertTrue("Unable to create sender onion api interface! " + e.getMessage(), false);
                }

                apiInterface.setSender();
                bind(ConfigurationProvider.class).toInstance(config);
                bind(OnionApiParser.class).to(OnionApiParserImpl.class);
                bind(OnionApiInterface.class).toInstance(apiInterface);

            }
        };

        receiverModule = new AbstractModule() {
            @Override
            protected void configure() {
                ConfigurationProvider config = null;
                OnionApiInterfaceMock apiInterface = null;
                try {
                    config = new ConfigurationProviderMock(5001,
                            6001,
                            7001,
                            9001,
                            1,
                            "localhost",
                            "localhost",
                            "localhost",
                            "localhost");
                } catch (UnknownHostException e) {
                    assertTrue("Unable to create receiver configuration! " + e.getMessage(), false);
                }
                try {
                    apiInterface = new OnionApiInterfaceMock();
                } catch (IOException e) {
                    assertTrue("Unable to create receiver onion api interface! " + e.getMessage(), false);
                }

                bind(ConfigurationProvider.class).toInstance(config);
                bind(OnionApiParser.class).to(OnionApiParserImpl.class);
                bind(OnionApiInterface.class).toInstance(apiInterface);

            }
        };

        ihModule = new AbstractModule() {
            @Override
            protected void configure() {
                ConfigurationProvider config = null;
                OnionApiInterfaceMock apiInterface = null;
                try {
                    config = new ConfigurationProviderMock(5002,
                            6002,
                            7002,
                            9002,
                            1,
                            "localhost",
                            "localhost",
                            "localhost",
                            "localhost");
                } catch (UnknownHostException e) {
                    assertTrue("Unable to create intermediate hop configuration! " + e.getMessage(), false);
                }
                try {
                    apiInterface = new OnionApiInterfaceMock();
                } catch (IOException e) {
                    assertTrue("Unable to create intermediate hop onion api interface! " + e.getMessage(), false);
                }

                apiInterface.setIntermediate();
                bind(ConfigurationProvider.class).toInstance(config);
                bind(OnionApiParser.class).to(OnionApiParserImpl.class);
                bind(OnionApiInterface.class).toInstance(apiInterface);

            }
        };

        senderInjector = Guice.createInjector(senderModule).createChildInjector(new OrchestratorUnitTestBaseInjector());
        receiverInjector = Guice.createInjector(receiverModule).createChildInjector(new OrchestratorUnitTestBaseInjector());
        ihInjector = Guice.createInjector(ihModule).createChildInjector(new OrchestratorUnitTestBaseInjector());
    }
}
