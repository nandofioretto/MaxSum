package agent.MaxSum;

import communication.ComAgent;
import communication.DCOPagent;
import communication.FunctionNode;
import communication.VariableNode;
import kernel.*;

import java.util.List;

/**
 * Created by nandofioretto on 5/15/17.
 */
public class MaxSumAgent extends DCOPagent {

    private int nbCycles = Integer.MAX_VALUE;
    private int currCycle = 0;
    private double convergenceDelta = 0.001;

    // The list of function nodes and variable nodes owned by this agent
    private List<FunctionNode> functionNodes;
    private List<VariableNode> variableNodes;

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

//    public class FactorGraphBuilder {
//        FactorGraphBuilder(AgentState agentState) {
//
//            // Create variable nodes
//            for (Variable v : agentState.getVariables()) {
//                variableNodes.add(new VariableNode(getSelf(), v));
//            }
//
//            // Create Function nodes
//            for (Constraint c : agentState.getConstraints()) {
//                if (isOwner(c)) {
//                    FunctionNode f = new FunctionNode(getSelf(), c);
//
//                    // Add varialbes nodes to function node as its neighbors
//                    for (VariableNode vn : variableNodes) {
//                        f.addNeighbor(vn);
//                    }
//                    functionNodes.add(f);
//                }
//            }
//        }
//
//        /** Returns true if this agent has ID = min of all IDs of agents involved in the scope of this constraint. */
//        public boolean isOwner(Constraint c) {
//            boolean ownership = true;
//            for (Variable v : c.getScope())
//                if (getId() > v.getOwnerAgent().getID())
//                    ownership = false;
//        }
//
//
//    }//

}//
