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
    private static AbstractModule senderModule, receiverModule, ih1Module, ih2Module;
    private static Injector senderInjector, receiverInjector, ih1Injector, ih2Injector;

    private static OrchestratorTestExtension sender, ih1, ih2, receiver;
    private static OnionApiInterfaceMock senderCM, ih1CM, ih2CM, receiverCM;

    @BeforeClass
    public static void initPeers() throws Exception {
        createInjectors();

        sender = senderInjector.getInstance(OrchestratorTestExtension.class);
        receiver = receiverInjector.getInstance(OrchestratorTestExtension.class);
        ih1 = ih1Injector.getInstance(OrchestratorTestExtension.class);
        ih2 = ih2Injector.getInstance(OrchestratorTestExtension.class);
    }



    @Test(timeout = 25000)
    public void testOrchestrator() throws InterruptedException {
        Throwable[] exc = new Throwable[1];
        exc[0] = null;

        ih1.start(false);
        ih2.start(false);
        receiver.start(false);
        sender.start(true);

        Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                exc[0] = e;
            }
        };

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Set<Thread> testThreads = new HashSet<>();
        for (Thread t : threadSet) {
            t.setUncaughtExceptionHandler(handler);
            if(t.getName().startsWith("Timer-"))
                testThreads.add(t);
        }

        receiverCM.testRunnerReady = true;
        ih1CM.testRunnerReady = true;
        ih2CM.testRunnerReady = true;
        senderCM.testRunnerReady = true;

        while(exc[0] == null) {
            Thread.sleep(100);
        }
        if(!"TESTS COMPLETED!".equals(exc[0].getMessage())) {
            exc[0].printStackTrace();
            fail(exc[0].getMessage());
        }
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
                            "localhost",
                            60);
                } catch (UnknownHostException e) {
                    assertTrue("Unable to create sender configuration! " + e.getMessage(), false);
                }
                try {
                    apiInterface = new OnionApiInterfaceMock();
                } catch (IOException e) {
                    assertTrue("Unable to create sender onion api interface! " + e.getMessage(), false);
                }

                apiInterface.setSender();
                senderCM = apiInterface;
                bind(ConfigurationProvider.class).toInstance(config);
                bind(OnionApiParser.class).to(OnionApiParserImpl.class);
                bind(OnionApiInterface.class).toInstance(apiInterface);

            }
        };

        ih1Module = new AbstractModule() {
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
                            "localhost",
                            60);
                } catch (UnknownHostException e) {
                    assertTrue("Unable to create receiver configuration! " + e.getMessage(), false);
                }
                try {
                    apiInterface = new OnionApiInterfaceMock();
                } catch (IOException e) {
                    assertTrue("Unable to create receiver onion api interface! " + e.getMessage(), false);
                }

                ih1CM = apiInterface;
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
                    config = new ConfigurationProviderMock(5002,
                            6002,
                            7002,
                            9002,
                            1,
                            "localhost",
                            "localhost",
                            "localhost",
                            "localhost",
                            60);
                } catch (UnknownHostException e) {
                    assertTrue("Unable to create intermediate hop configuration! " + e.getMessage(), false);
                }
                try {
                    apiInterface = new OnionApiInterfaceMock();
                } catch (IOException e) {
                    assertTrue("Unable to create intermediate hop onion api interface! " + e.getMessage(), false);
                }

                apiInterface.setIntermediate();
                receiverCM = apiInterface;
                bind(ConfigurationProvider.class).toInstance(config);
                bind(OnionApiParser.class).to(OnionApiParserImpl.class);
                bind(OnionApiInterface.class).toInstance(apiInterface);

            }
        };

        ih2Module = new AbstractModule() {
            @Override
            protected void configure() {
                ConfigurationProvider config = null;
                OnionApiInterfaceMock apiInterface = null;
                try {
                    config = new ConfigurationProviderMock(5002,
                            6003,
                            7003,
                            9003,
                            1,
                            "localhost",
                            "localhost",
                            "localhost",
                            "localhost",
                            60);
                } catch (UnknownHostException e) {
                    assertTrue("Unable to create intermediate hop configuration! " + e.getMessage(), false);
                }
                try {
                    apiInterface = new OnionApiInterfaceMock();
                } catch (IOException e) {
                    assertTrue("Unable to create intermediate hop onion api interface! " + e.getMessage(), false);
                }

                apiInterface.setIntermediate();
                ih2CM = apiInterface;
                bind(ConfigurationProvider.class).toInstance(config);
                bind(OnionApiParser.class).to(OnionApiParserImpl.class);
                bind(OnionApiInterface.class).toInstance(apiInterface);

            }
        };

        senderInjector = Guice.createInjector(senderModule).createChildInjector(new OrchestratorUnitTestBaseInjector());
        receiverInjector = Guice.createInjector(receiverModule).createChildInjector(new OrchestratorUnitTestBaseInjector());
        ih1Injector = Guice.createInjector(ih1Module).createChildInjector(new OrchestratorUnitTestBaseInjector());
        ih2Injector = Guice.createInjector(ih2Module).createChildInjector(new OrchestratorUnitTestBaseInjector());
    }
}
