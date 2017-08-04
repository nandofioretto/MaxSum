package communication;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import kernel.AgentState;
import kernel.Constants;
import kernel.DCOPinfo;

import java.util.*;

/**
 * Created by nando on 6/2/17.
 */
public class CycleTickerDeamon /*extends ComAgent*/ {

    int currentCycle;
    int nbAgents;
    BitSet agentsTerminatedCurrentCycle;

    public CycleTickerDeamon(List<AgentState> spawnedAgentStates) {
        currentCycle = 0;
        nbAgents = spawnedAgentStates.size();
        agentsTerminatedCurrentCycle = new BitSet(spawnedAgentStates.size());
    }

    public synchronized void terminateAgentCycle(ComAgent agent) {
        //System.out.println(agent.getName() + " Terminating current cycle");
        agent.getAgentStatistics().updateIterationStats();
        agent.setAgtState(ComAgent.STOPPED);
        agentsTerminatedCurrentCycle.set((int)agent.getId());
        if (agentsTerminatedCurrentCycle.cardinality() == nbAgents)
        {
            // compute cost
            System.out.println("Solution Cost at iter: " + currentCycle + " : " + Constants.printValue(getProblemCost()));
            currentCycle ++;
            agentsTerminatedCurrentCycle.clear();

            // start new cycle by sending each message
            for (DCOPagent agt : DCOPinfo.agentsRef.values()) {
                agt.setAgtState(ComAgent.RUNNING);
                agt.tell(new Message.StartNewCycle(), ComAgent.noSender());
            }
        }
    }


    public double getProblemCost() {
        double cost = 0;
        for (DCOPagent agt : DCOPinfo.agentsRef.values()) {
            double val = agt.getAgentView().getEvaluator().evaluate();
            if (Constants.isInf(val)) {
                return Constants.infinity;
            }
            cost += val;
        }
        return cost;
    }

    public int getCurrentCycle() {
        return currentCycle;
    }
}
