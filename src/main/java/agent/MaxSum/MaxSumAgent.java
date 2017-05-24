package agent.MaxSum;

import agent.FactorGraphAgent;
import communication.ComAgent;
import communication.FunctionNode;
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

    private HashMap<Long, HashMap<Long, double[] >> vnTofnValues;
    private HashMap<Long, HashMap<Long, double[] >> fnTovnValues;

    public MaxSumAgent(ComAgent statsCollector, AgentState agentState, List<Object> parameters) {
        super(statsCollector, agentState);

        // Check argument:
        assert(parameters.size() == 1);
        this.nbCycles = (int)parameters.get(0);

        vnTofnValues = new HashMap<>();
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

        // Initialize NodesToFactor Values
        // todo: Delegate this to a class (this is a responsivility of the variable node, or MaxSumAgentState)
        for (VariableNode vnode : getVariableNodes()) {
            long vnodeId = vnode.getID();
            int vDomSize = vnode.getVariable().getDomain().size();
            vnTofnValues.put(vnodeId, new HashMap<>());
            for (FunctionNode fnode : vnode.getNeighbors()) {
                long fnodeId = fnode.getID();
                double vInit[] = new double[vDomSize];
                Arrays.fill(vInit, 0.0);
                vnTofnValues.get(vnodeId).put(fnodeId, vInit);
            }
        }

        // Initialize FactorToVariables Values
        // todo: Delegate this to a class (this is a responsivility of the variable node, or MaxSumAgentState)
        for (FunctionNode fnode : getFunctionNodes()) {
            long fnodeId = fnode.getID();
            fnTovnValues.put(fnodeId, new HashMap<>());
            for (VariableNode vnode : fnode.getNeighbors()) {
                long vnodeId = fnode.getID();
                int vDomSize = vnode.getVariable().getDomain().size();
                double vInit[] = new double[vDomSize];
                Arrays.fill(vInit, 0.0);
                fnTovnValues.get(fnodeId).put(vnodeId, vInit);
            }
        }


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
            String s  = "FunctionToVariableMessage: [";
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
                String s  = "VariableToFunctionMessage: [";
                for (double d : values)
                    s += d + " ";
                s += "]";
                return s;
            }

        }

}//
