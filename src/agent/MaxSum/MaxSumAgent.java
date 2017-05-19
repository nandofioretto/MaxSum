package agent.MaxSum;

import agent.FactorGraphAgent;
import communication.ComAgent;
import communication.DCOPagent;
import communication.FunctionNode;
import communication.VariableNode;
import kernel.*;

import java.util.List;

/**
 * Created by nandofioretto on 5/15/17.
 */
public class MaxSumAgent extends FactorGraphAgent {

    private int nbCycles = Integer.MAX_VALUE;
    private int currCycle = 0;
    private double convergenceDelta = 0.001;


    public MaxSumAgent(ComAgent statsCollector, AgentState agentState, List<Object> parameters) {

        super(statsCollector, agentState);
        this.nbCycles = (int)parameters.get(0);
    }

    @Override
    protected boolean terminationCondition() {
        return currCycle >= nbCycles;
    }

    @Override
    protected void onStart() {
        // Assign variable value to 0
        getAgentActions().setVariableValue(0);
        long aId = getAgentView().getAgentID();
    }

}//
