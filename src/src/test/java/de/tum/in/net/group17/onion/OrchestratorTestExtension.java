package de.tum.in.net.group17.onion;

import de.tum.in.net.group17.onion.interfaces.onionapi.OnionApiInterfaceMock;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Marko Dorfhuber(PraMiD) on 12.08.17.
 *
 * This class is used for UnitTests only.
 * We use this inheritance of the Orchestrator to have the possibility to issue a round transition by hand in the unit
 * test.
 * THE MAIN FUNCTIONALITY OF THE ORCHESTRATOR MUST NOT BE CHANGED BY THIS SUBCLASS.
 */
public class OrchestratorTestExtension extends Orchestrator {

    public OrchestratorTestExtension() {
        super();

        // Avoid that a new cover tunnel is build before we issue the round transition during the test
        // => RPS mock would get too complex
        this.ROUND_START_DELAY = 5000;
    }

    /**
     * This method provides the possibility to manually issue a round transition on the initiator for testing purposes.
     */
    public void issueRoundTransition() {
        try {
            Field f = Orchestrator.class.getDeclaredField("roundTask");
            f.setAccessible(true);
            ((TimerTask) f.get(this)).run();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Should not happen..
            throw new RuntimeException("Cannot access roundTask to issue the manual round transition: " + e.getMessage());
        }
    }

    /**
     * This method has the exact same behavior as in the 'real' Orchestrator. However, it provides the possibility
     * to disable round handling. This is necessary on the receiver and intermediate hop for testing.
     *
     * @inheritDoc
     *
     * @param initiator We will disable round handling if the peer is not the initiator.
     */
    public void start(boolean initiator) {
        super.start();

        try {
            Field f = Orchestrator.class.getDeclaredField("apiInterface");
            f.setAccessible(true);

            if(f.get(this) instanceof OnionApiInterfaceMock) {
                ((OnionApiInterfaceMock)f.get(this)).setOrchestratorInstance(this);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Should not happen..
            throw new RuntimeException("Cannot access apiInterface to set the orchestrator instance: " + e.getMessage());
        }


                    // The intermediate hop and the receiver should only respond and not start rounds on their own..
        if(!initiator) {
            try {
                Field f = Orchestrator.class.getDeclaredField("roundTimer");
                f.setAccessible(true);
                ((Timer) f.get(this)).cancel();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Should not happen..
                throw new RuntimeException("Cannot access field in OrchestratorTestExtension constructor to disable " +
                        "rounds on intermediate hop and receiver: " + e.getMessage());
            }
        }
    }
}
