package agent.MaxSum;

import agent.FactorGraphAgent;
import communication.ComAgent;
import communication.FactorNode;
import communication.Message;
import communication.VariableNode;
import kernel.AgentState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nandofioretto on 5/15/17.
 */
public class MaxSumAgent extends FactorGraphAgent {

    private int nbCycles = Integer.MAX_VALUE;
    private int currCycle = 0;
    private double convergenceDelta = 0.001;

    private List<MaxSumVariableNode> variableNodes;
    private List<MaxSumFactorNode> factorNodes;

    // key = varID; value = variableNodes index associated to that variable ID
    private HashMap<Long, Integer> mapVarPos;

    public MaxSumAgent(ComAgent statsCollector, AgentState agentState, List<Object> parameters) {
        super(statsCollector, agentState);

        // Check argument:
        assert (parameters.size() == 1);
        this.nbCycles = (int) parameters.get(0);

        variableNodes = new ArrayList<>();
        factorNodes = new ArrayList<>();
        mapVarPos = new HashMap<>();
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

        // Initialize MaxSumVariableNodes
        for (VariableNode vnode : getVariableNodes()) {
            variableNodes.add(new MaxSumVariableNode(vnode));

            int vIdx = findVariableID(vnode.getVariable().getID());
            mapVarPos.put(vnode.getID(), vIdx);
        }

        // Initialize MaxSumFactorNodes
        for (FactorNode fnode : getFactorNodes()) {
            factorNodes.add(new MaxSumFactorNode(fnode));
        }
        cycle();
    }

    synchronized private void cycle() {
        // Select best value from all the variables controlled by this agent by calling the routines in variable nodes
        for (MaxSumVariableNode vnode : variableNodes) {
            int val = vnode.selectBestValue();
            getAgentActions().setVariableValue(mapVarPos.get(vnode.getID()), val);
        }
        // Send Messages from a variable node to neighbor factors nodes
        // see Line 151 of VariableNode.java Liel

        // Send Messages from a factor node to neighbor variable nodes
    }

    /// Auxiliary Functions
    private int findVariableID(long id) {
        for (int i = 0; i < getAgentView().getNbVariables(); i++) {
            long vId = getAgentView().getVariableId(i);
            if (vId == id)
                return i;
        }
        assert(true);
        return -1;
    }

    public static class FunctionToVariableMessage extends Message {
        private ArrayList<Double> values = new ArrayList<>();

        public FunctionToVariableMessage(ArrayList<Double> values) {
            this.values = values;
        }

        public ArrayList<Double> getValues() {
            return values;
        }

        @Override
        public String toString() {
            String s = "FunctionToVariableMessage: [";
            for (double d : values)
                s += d + " ";
            s += "]";
            return s;
        }

        public static class VariableToFunctionMessage extends Message {
            private ArrayList<Double> values = new ArrayList<>();

            public VariableToFunctionMessage(ArrayList<Double> values) {
                this.values = values;
            }

            public ArrayList<Double> getValues() {
                return values;
            }

            @Override
            public String toString() {
                String s = "VariableToFunctionMessage: [";
                for (double d : values)
                    s += d + " ";
                s += "]";
                return s;
            }

        }
    }//-

}
//
