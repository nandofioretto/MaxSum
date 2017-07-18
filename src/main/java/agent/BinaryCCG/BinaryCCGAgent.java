package agent.BinaryCCG;

import agent.SynchronousAgent;
import communication.BasicMessage;
import communication.ComAgent;
import kernel.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fioretto on 7/17/17.
 */
public class BinaryCCGAgent extends SynchronousAgent {

    private int nbCycles = Integer.MAX_VALUE;
    private double convergenceDelta = 0.001;
    private int nbRecvMsgs;
    private int nbVarsNeighbor;

    // Cost received by each neighbor by projecting out y if this variable is x [prev cycle]
    // key: variable ID; value: vector of size Dom of this variable
    // todo: Check here -> variable IDs as Keys or Agnet IDs?
    private HashMap<Long, double[]> costTables;

    // Cost received by each neighbor  at current cycle
    // key: variable ID; value: vector of size Dom of this variable
    private HashMap<Long, double[]> recvCostTables;

    // Weights of the unary constraint
    private double weight;

    // A vector of noisy values to allow faster convergence
    public double[] noise;

    public BinaryCCGAgent(ComAgent statsCollector, AgentState agentState, List<Object> parameters) {
        super(statsCollector, agentState);
        // Check argument:
        assert (parameters.size() == 1);
        // check binary
        assert(getAgentView().getDomainSize() == 2);

        this.nbCycles = (int) parameters.get(0);
        this.costTables = new HashMap<>();
        this.recvCostTables = new HashMap<>();
        this.nbRecvMsgs = 0;
        this.nbVarsNeighbor = 0;
        this.weight = 0;

        Tuple tuple = new Tuple(new int[]{1});
        for (Constraint c: agentState.getConstraints())
            if (c.isUnary() && c.getScope().get(0) == agentState.getVariable())
                this.weight = c.getValue(tuple);

        // Initialize messages
        double[] costs = new double[getAgentView().getDomainSize()];
        Arrays.fill(costs, 0);
        for (AgentState n : agentState.getNeighbors()) {
            for (Variable v : n.getVariables()) {
                costTables.put(v.getID(), costs.clone());
                this.nbVarsNeighbor++;
            }
        }

        this.noise = new  double[2];
        for (int i = 0; i <noise.length; i++) {
            this.noise[i] = Math.random();
        }
    }


    @Override
    protected void onStart() {
        getAgentActions().setVariableValue(0);

        // start cycling
        super.onStart();
    }

    @Override
    protected void onStop() {
        double w0 = 0, w1 = 0;
        boolean converged = true;
        for (ComAgent n : getNeighborsRef()) {
            w0 += recvCostTables.get(n.getId())[0];
            w1 += recvCostTables.get(n.getId())[1];
        }
        w1 += weight;
        if (Constants.isInf(w0) || Constants.isInf(w1)) {
            converged = false;
        }
        if (getAgentView().getVariableType() == Variable.DEF_TYPE) {
            getAgentActions().setVariableValue(w0 > w1 ? 1 : 0);
        }
        else
            getAgentActions().setVariableValue(-1);

        System.out.println("Agent " + getName() + " on Stop -- select value: " +
                getAgentView().getVariableValue() );

        super.onStop();
    }

    @Override
    protected void onReceive(Object message, ComAgent sender) {
        super.onReceive(message, sender);

        if (message instanceof CCGTableMessage) {
            CCGTableMessage msg = (CCGTableMessage)message;
            recvCostTables.put(msg.getVarId(), msg.getTable());
            nbRecvMsgs++;

            System.out.println(getName() + " received " + message.toString()
                    + "  # msg recv: " + nbRecvMsgs + " / " + nbVarsNeighbor);

            if (nbRecvMsgs == nbVarsNeighbor) {
                terminateCycle();
            }
        }
    }

    @Override
    protected void cycle() {
        // Send messages
        // todo check if onwner of received messages is same as this agent
        for (ComAgent k : getNeighborsRef()) {
            double[] table = getCostTableSumExcluding(k.getId());
            //Commons.rmValue(table, Commons.getAverage(table));
            Commons.rmValue(table, Commons.getMin(table));
            //Commons.addArray(table, noise);

            CCGTableMessage msg = new CCGTableMessage(table, getAgentView().getVariableId());
            k.tell(msg, getSelf());
        }
    }

    @Override
    protected void onCycleStart() {
        int val = selectBestValue();
        getAgentActions().setVariableValue(val);

        System.out.println("Agent " + getName() + " Starting cycle: " + getCurrentCycle() +
                " select value: " + getAgentView().getVariableValue() );
    }

    @Override
    protected void onCycleEnd() {
        nbRecvMsgs = 0;

        // Save all received messages to be used in the next iteration
        for (Map.Entry<Long, double[]> entry : recvCostTables.entrySet()) {
            Long key = entry.getKey();
            double[] value = entry.getValue();
            costTables.put(key, value.clone());
        }

        System.out.println("Agent " + getName() + " Terminating cycle  " + getCurrentCycle());
    }

    @Override
    protected boolean terminationCondition() {
        return getCurrentCycle() >= nbCycles;
    }

    /**
     * Returns the aggregated cost table, excluding the costs produced by this agent
     */
    private double[] getCostTableSumExcluding(long excludedId) {
        double[] sum = new double[]{weight, 0};
        double[] s = getCostTableSum(excludedId);
        sum[0] = s[0] + weight;
        sum[1] = Math.min(s[0], s[1] + weight);
        return sum;
    }

    private double[] getCostTableSum(long excludedId) {
        double[] sum = new double[]{0,0};
        for (ComAgent k : getNeighborsRef()) {
            if (k.getId() == excludedId)
                continue;
            Commons.addArray(sum, costTables.get(k.getId()));
        }
        return sum;
    }

    private int selectBestValue() {
        double[] table = getCostTableSum(-1);
        table[1] += weight;
        int val_idx = Commons.getArgMin(table);
        return getAgentView().getDomainElement(val_idx);
    }

    /// Messages ----------------------- //
    public static class CCGTableMessage extends BasicMessage {
        protected double[] table;
        private long varId ;

        public CCGTableMessage(double[] table, long vId) {
            this.table = table;
            this.varId = vId;
        }

        public double[] getTable() {
            return table;
        }

        public long getVarId() {
            return varId;
        }

        @Override
        public String toString() {
            return "CCGTableMessage{" +
                    "table=" + Arrays.toString(table) +
                    ", varId=" + varId +
                    '}';
        }
    }
}
