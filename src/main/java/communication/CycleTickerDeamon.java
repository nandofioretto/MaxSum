package communication;

import kernel.AgentState;
import kernel.DCOPinfo;

import java.util.*;

/**
 * Created by nando on 6/2/17.
 */
public class CycleTickerDeamon /*extends ComAgent*/ {

    int currentCycle;
    int nbAgents;
    //List<Boolean> in_queue_list = Collections.synchronizedList(in_queue);
    BitSet agentsTerminatedCurrentCycle;

    public CycleTickerDeamon(List<AgentState> spawnedAgentStates) {
        currentCycle = 0;
        nbAgents = spawnedAgentStates.size();
        agentsTerminatedCurrentCycle = new BitSet(spawnedAgentStates.size());
    }

    public synchronized void terminateAgentCycle(ComAgent agent) {
        agentsTerminatedCurrentCycle.set((int)agent.getId());

        if (agentsTerminatedCurrentCycle.cardinality() == nbAgents)
        {
            currentCycle ++;
            agentsTerminatedCurrentCycle.clear();

            // start new cycle by sending each message
            for (DCOPagent agt : DCOPinfo.agentsRef.values())
                agt.tell(new Message.StartNewCycle(), ComAgent.noSender());
        }
    }

    public int getCurrentCycle() {
        return currentCycle;
    }
}
