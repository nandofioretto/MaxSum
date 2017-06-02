package agent.MaxSum;

import communication.FactorNode;
import communication.VariableNode;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by nando on 5/24/17.
 */
public class MaxSumFactorNode {

    private FactorNode node;

    // Cost received by each variable participating to this factor.
    // key: variable ID; value: vector of size Dom of variable

    // todo: We assume now that all constraints are binary
    private HashMap<Long, double[]> costTable;

    public MaxSumFactorNode (FactorNode node) {
        this.node = node;
        costTable = new HashMap<>();

        for (VariableNode v : node.getNeighbors()) {
            double[] costs = new double[v.getVariable().getDomain().size()];
            Arrays.fill(costs, 0);
            costTable.put(v.getID(), costs);
        }
    }


    public void copyCostTable(double[] table, long fNodeId) {
        // todo: don't need to clone the table here
        costTable.put(fNodeId, table.clone());
    }


}
