package agent.MaxSum;

import agent.FactorGraphAgent;
import communication.BasicMessage;
import communication.ComAgent;
import communication.FactorNode;
import communication.VariableNode;
import kernel.AgentState;

import java.util.*;

/**
 * Created by nandofioretto on 5/15/17.
 */
public class MaxSumAgent extends FactorGraphAgent {

    private int nbCycles = Integer.MAX_VALUE;
    private int currCycle = -1;
    private int cycleModule = 100;
    private double convergenceDelta = 0.001;

    // key: variableNode ID, value = maxSumNode
    private Map<Long, MaxSumVariableNode> variableNodes;
    private Map<Long, MaxSumFactorNode> factorNodes;

    // Message counts (MAKE THIS A CLASS Msg Manager)
    private int[] nbRecvFmsgs;
    // the total number of variable node neighbors (of all functions which this agent expects to receive messages from)
    // i.e., with ID > this agent ID
    int totalNbVneibgbors = 0;
    private int[] nbRecvVmsgs;
    // the total number of factor node neighbors (of all functions which this agent expects to receive messages from)
    // [i.e., with ID > this agent ID]
    int totalNbFneibgbors = 0;

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
            totalNbFneibgbors += vnode.getHigherPriorityNeighbors().size();
        }

        // Initialize MaxSumFactorNodes
        for (FactorNode fnode : getFactorNodes()) {
            factorNodes.put(fnode.getID(), new MaxSumFactorNode(fnode));
            totalNbVneibgbors += fnode.getNeighbors().size();
        }

        // Initialize the received message count

        nbRecvFmsgs = new int[cycleModule];
        Arrays.fill(nbRecvFmsgs, 0);
        nbRecvVmsgs = new int[cycleModule];
        Arrays.fill(nbRecvVmsgs, 0);
        cycle();
    }

    @Override
    protected void onReceive(Object message, ComAgent sender) {
        super.onReceive(message, sender);

        // 2. Implment this logic
        if (message instanceof VnodeToFnodeMessage) {
            VnodeToFnodeMessage msg = (VnodeToFnodeMessage)message;
            long fnodeId = msg.getFactorNodeID();
            long vNodeId = msg.getVariableNodeID();
            factorNodes.get(fnodeId).copyCostTable(msg.getTable(), vNodeId);
            nbRecvVmsgs[msg.getCycleNo()] ++;
        }
        else if (message instanceof FnodeToVnodeMessage) {
            FnodeToVnodeMessage msg = (FnodeToVnodeMessage)message;
            long fNodeId = msg.getFactorNodeID();
            long vNodeId = msg.getVariableNodeID();
            variableNodes.get(vNodeId).copyCostTable(msg.getTable(), fNodeId);
            nbRecvFmsgs[msg.getCycleNo()] ++;
        }

        if (nbRecvVmsgs[currCycle] == totalNbVneibgbors && nbRecvFmsgs[currCycle] == totalNbFneibgbors) {
            cycle();
        }
    }

    private void cycle() {
        currCycle++;
        resetNbMessageReceived( (currCycle - 50) % cycleModule);

        // Select best value from all the variables controlled by this agent by calling the routines in variable nodes
        for (MaxSumVariableNode vnode : variableNodes.values()) {
            int val = vnode.selectBestValue();
            getAgentActions().setVariableValue(mapVarPos.get(vnode.getID()), val);

            // VARIABLE-NODE LOGIC
            vnode.sendMessages(currCycle);
        }
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

    private void resetNbMessageReceived(int iter) {
        nbRecvFmsgs[iter] = 0;
        nbRecvVmsgs[iter] = 0;
    }

    /// Messages ------------------------------- //
    public static class TableMessage extends BasicMessage {
        protected double[] table;
        protected long fNodeId; // sender factor node
        protected long vNodeId; // receiver factor node
        protected int cycleNo;

        public TableMessage(double[] table, long vNodeId, long fNodeId, int currCycle) {
            this.table = table.clone();
            this.vNodeId = vNodeId;
            this.fNodeId = fNodeId;
            this.cycleNo = currCycle;
        }

        public long getFactorNodeID() {
            return fNodeId;
        }

        public long getVariableNodeID() {
            return vNodeId;
        }

        public int getCycleNo() {
            return cycleNo;
        }

        public double[] getTable() {
            return table;
        }

    }

    public static class VnodeToFnodeMessage extends TableMessage {
        /**
         * A Variable to Factor node message
         * @param table The cost table
         * @param vNodeId sender (variable) node ID
         * @param fNodeId receiver (factor) node ID
         * @param currCycle the sender cycle number
         */
        public VnodeToFnodeMessage(double[] table, long vNodeId, long fNodeId, int currCycle) {
            super(table, vNodeId, fNodeId, currCycle);
        }

        @Override
        public String toString() {
            String s = "["+ cycleNo +"]VnodeToFnodeMessage: vId " + vNodeId + " -> fId " + fNodeId + "[";
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
         * @param currCycle the sender cycle number
         */
        public FnodeToVnodeMessage(double[] table, long fNodeId, long vNodeId, int currCycle) {
            super(table, vNodeId, fNodeId, currCycle);
        }

        @Override
        public String toString() {
            String s = "["+ cycleNo +"]FnodeToVnodeMessage: fId " + fNodeId + " -> vId " + vNodeId + "[";
            for (double d : table) s += d + " ";
            return s + "]";
        }
    }
}
//
