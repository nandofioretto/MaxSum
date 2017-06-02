package agent;

import communication.ComAgent;
import communication.CycleTickerDeamon;
import communication.DCOPagent;
import communication.Message;
import kernel.AgentState;
import kernel.DCOPInstance;
import kernel.DCOPalgorithmState;
import kernel.DCOPinfo;

/**
 * Created by nando on 6/2/17.
 */
public abstract class SynchronousAgent extends DCOPagent {

    int currentCycle;
    CycleTickerDeamon cycleTickerDeamon;

    public SynchronousAgent(ComAgent statsCollector, AgentState agentState) {
        super(statsCollector, agentState);
        this.currentCycle = 0;
        cycleTickerDeamon = DCOPinfo.cycleTickerDeamon;
    }

    @Override
    protected void onReceive(Object message, ComAgent sender) {
        super.onReceive(message, sender);

        if (message instanceof Message.StartNewCycle) {
            currentCycle++;
        }
    }

    protected void terminateCycle() {
        cycleTickerDeamon.terminateAgentCycle(getSelf());
    }

}
