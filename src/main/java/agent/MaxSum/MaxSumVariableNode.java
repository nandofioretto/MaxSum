package agent.MaxSum;

import communication.VariableNode;
import communication.FactorNode;
import kernel.Domain;
import kernel.Variable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.NavigableMap;

/**
 * Created by nando on 5/24/17.
 */
public class MaxSumVariableNode {

    // node not needed - only need domain size
    private VariableNode node;

    // Cost received by each function node whose variable is participating to:
    // key: function ID; value: vector of size Dom of variable
    private HashMap<Long, double[]> costTable;
    boolean reading_cost_table = false;

    public MaxSumVariableNode(VariableNode node) {
        this.node = node;
        costTable = new HashMap<>();
        for (FactorNode f : node.getNeighbors()) {
            double[] costs = new double[node.getVariable().getDomain().size()];
            Arrays.fill(costs, 0);
            costTable.put(f.getID(), costs);
        }
    }

    public long getID() {
        return node.getID();
    }

    // todo: Add dust to have tie-breaker (see Liel code)
    public int selectBestValue() {
        double[] table = getCostTableSum();
        Domain dom = node.getVariable().getDomain();

        int val = 0;
        double cost = Double.MAX_VALUE;
        for (int d = 0; d < dom.size(); d++) {
            if (table[d] < cost) {
                cost = table[d];
                val = d;
            }
        }
        return val;
    }

    // NOTE: Check synchronization with methods writing in costTable
    private double[] getCostTableSum() {
        reading_cost_table = true;

        Domain dom = node.getVariable().getDomain();
        double[] sum = new double[dom.size()];
        for (int d = 0; d < dom.size(); d++) {
            sum[d] = 0;
            for (FactorNode f : node.getNeighbors()) {
                sum[d] += costTable.get(f.getID())[d];
            }
        }

        reading_cost_table = false;
        return sum;
    }


    private void resetCostTable() {
        for (FactorNode f : node.getNeighbors())
            Arrays.fill(costTable.get(f), 0.0);
    }

}
