package agent.MaxSum;

import agent.FactorGraphAgent;
import communication.*;
import kernel.AgentState;
import kernel.Commons;

import java.util.*;

/**
 * Created by nandofioretto on 5/15/17.
 */
public class MaxSumAgent extends FactorGraphAgent {

    private int nbCycles = Integer.MAX_VALUE;
    private int currCycle = 0;
    private double convergenceDelta = 0.001;

    // key: variableNode ID, value = maxSumNode
    private Map<Long, MaxSumVariableNode> variableNodes;
    private Map<Long, MaxSumFactorNode> factorNodes;

    // key = varID; value = variableNodes index associated to that variable ID
    private Map<Long, Integer> mapVarPos;

    public MaxSumAgent(ComAgent statsCollector, AgentState agentState, List<Object> parameters) {
        super(statsCollector, agentState);

        // Check argument:
        assert (parameters.size() == 1);
        this.nbCycles = (int) parameters.get(0);

        variableNodes = new TreeMap<>();
        factorNodes = new TreeMap<>();
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
            variableNodes.put(vnode.getID(), new MaxSumVariableNode(vnode));

            int vIdx = findVariableID(vnode.getVariable().getID());
            mapVarPos.put(vnode.getID(), vIdx);
        }

        // Initialize MaxSumFactorNodes
        for (FactorNode fnode : getFactorNodes()) {
            factorNodes.put(fnode.getID(), new MaxSumFactorNode(fnode));
        }
        cycle();
    }

    @Override
    protected void onReceive(Object message, ComAgent sender) {
        super.onReceive(message, sender);

        // 2. Implment this logic
        if (message instanceof VnodeToFnodeMessage) {
            VnodeToFnodeMessage msg = (VnodeToFnodeMessage)message;
            long fnodeId = msg.getFactorNodeID();
            //factorNodes.get(fnodeId)
        }
        else if (message instanceof FnodeToVnodeMessage) {
            FnodeToVnodeMessage msg = (FnodeToVnodeMessage)message;
            long fNodeId = msg.getFactorNodeID();
            long vNodeId = msg.getVariableNodeID();
            variableNodes.get(vNodeId).copyCostTable(msg.getTable(), fNodeId);
        }


        // todo 4. Implment this logic
//        if (all fNodeToVNodeMessage received and all vNodeTofNodeMessage received) {
//            cycle()
//        }

    }

    private void cycle() {

        // Select best value from all the variables controlled by this agent by calling the routines in variable nodes
        for (MaxSumVariableNode vnode : variableNodes.values()) {
            int val = vnode.selectBestValue();
            getAgentActions().setVariableValue(mapVarPos.get(vnode.getID()), val);

            // VARIABLE-NODE LOGIC
            // Send Messages from a factor node to neighbor variable nodes
            for (FactorNode fnode : vnode.node.getNeighbors()) {
                double[] table = vnode.getCostTableSumExcluding(fnode.getID());
                // todo LATER: addUnaryConstraints(table);
                Commons.rmValue(table, Commons.getMin(table));
                Commons.addArray(table, vnode.getNoise());

                // Send message: if factor agents = variable agent - simply copy table
                //               otherwhise send message
                if (fnode.getOwner().equals(this)) {
                    // todo: 5. copy table
                }
                else {
                    fnode.getOwner().tell(new VnodeToFnodeMessage(table, vnode.getID(), fnode.getID()), getSelf());
                }
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

    // Messages
    public class TableMessage extends BasicMessage {
        protected double[] table;
        long fNodeId; // sender factor node
        long vNodeId; // receiver factor node

        public TableMessage(double[] table, long vNodeId, long fNodeId)) {
            this.table = table.clone();
            this.vNodeId = vNodeId;
            this.fNodeId = fNodeId;
        }

        public long getFactorNodeID() {
            return fNodeId;
        }

        public long getVariableNodeID() {
            return vNodeId;
        }

        public double[] getTable() {
            return table;
        }

    }

    public class VnodeToFnodeMessage extends TableMessage {
        /**
         * A Variable to Factor node message
         * @param table The cost table
         * @param vNodeId sender (variable) node ID
         * @param fNodeId receiver (factor) node ID
         */
        public VnodeToFnodeMessage(double[] table, long vNodeId, long fNodeId) {
            super(table, vNodeId, fNodeId);
        }

        @Override
        public String toString() {
            String s = "VnodeToFnodeMessage: vId " + vNodeId + " -> fId " + fNodeId + "[";
            for (double d : table) s += d + " ";
            return s + "]";
        }
    }

    public class FnodeToVnodeMessage extends TableMessage {
        /**
         * A Variable to Factor node message
         * @param table The cost table
         * @param fNodeId sender (factor) node ID
         * @param vNodeId receiver (variable) node ID
         */
        public FnodeToVnodeMessage(double[] table, long fNodeId, long vNodeId) {
            super(table, vNodeId, fNodeId);
        }

        @Override
        public String toString() {
            String s = "FnodeToVnodeMessage: fId " + fNodeId + " -> vId " + vNodeId + "[";
            for (double d : table) s += d + " ";
            return s + "]";
        }
    }
}
//
