package de.tum.in.net.group17.onion;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.tum.in.net.group17.onion.interfaces.authentication.AuthenticationInterface;
import de.tum.in.net.group17.onion.interfaces.onion.OnionInterface;
import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterface;
import de.tum.in.net.group17.onion.interfaces.rps.RandomPeerSamplingInterface;

/**
 * This class orchestrates the flow of data between various interfaces.
 * It also acts as clock to manage rounds.
 * Created by Christoph Rudolf on 09.07.17.
 */
public class Orchestrator {
    @Inject
    private RandomPeerSamplingInterface rpsInterface;
    @Inject
    private AuthenticationInterface authInterface;
    @Inject
    private OnionApiInterface apiInterface;
    @Inject
    private OnionInterface onionInterface;

    public static void main(String[] args) {
        System.out.println("Starting up!");

        // Setup the dependency injection with Guice
        Injector injector = Guice.createInjector(new ProductionInjector());
        Orchestrator orchestrator = injector.getInstance(Orchestrator.class);
        orchestrator.start();
    }

    public void start() {
        // Test if injection worked and it is not null
        System.out.println(rpsInterface.toString());
    }




}
