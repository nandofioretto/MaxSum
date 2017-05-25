package agent.MaxSum;

import agent.FactorGraphAgent;
import communication.*;
import kernel.AgentState;
import kernel.Commons;

import java.util.ArrayList;
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

            // VARIABLE NODE LOGIC
            // todo: (MOVE THE FOLLOWING TO MAXSUM FACTORNODE)
            // Send Messages from a factor node to neighbor variable nodes
            for (FactorNode fnode : vnode.node.getNeighbors()) {
                double[] table = vnode.getCostTableSumExcluding(fnode.getID());
                // todo: addUnaryConstraints(table);
                Commons.rmValue(table, Commons.getMin(table));
                Commons.addArray(table, vnode.getNoise());
                fnode.getOwner().tell(new VnodeToFnodeMessage(table), getSelf());
            }

        }
        currCycle++;
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

    @Override
    protected void onReceive(Object message, ComAgent sender) {
        super.onReceive(message, sender);

        if (message instanceof VnodeToFnodeMessage) {

        }
        else if (message instanceof FnodeToVnodeMessage) {

        }
    }

    // Messages
    public static class TableMessage extends BasicMessage {
        protected double[] table;

        public TableMessage(double[] table) {
            this.table = table.clone();
        }

        public double[] getTable() {
            return table;
        }
    }

    public static class VnodeToFnodeMessage extends TableMessage {
        public VnodeToFnodeMessage(double[] table) {
            super(table);
        }

        @Override
        public String toString() {
            String s = "VnodeToFnodeMessage: [";
            for (double d : table)
                s += d + " ";
            return s + "]";
        }
    }

    public static class FnodeToVnodeMessage extends TableMessage {
        public FnodeToVnodeMessage(double[] table) {
            super(table);
        }

        @Override
        public String toString() {
            String s = "FnodeToVnodeMessage : [";
            for (double d : table)
                s += d + " ";
            return s + "]";
        }

    }
}
//
