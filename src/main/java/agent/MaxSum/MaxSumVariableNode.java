package agent.MaxSum;

import communication.FactorNode;
import communication.VariableNode;
import kernel.Commons;
import kernel.Domain;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by nando on 5/24/17.
 */
public class MaxSumVariableNode {

    // node not needed - only need domain size
    public VariableNode node;

    // Cost received by each function node whose variable is participating to:
    // key: functionNode ID; value: vector of size Dom of variable
    private HashMap<Long, double[]> costTable;

    // A vector of noisy values to allow faster convergence
    public double[] noise;

    boolean reading_cost_table = false;

    public MaxSumVariableNode(VariableNode node) {
        this.node = node;
        costTable = new HashMap<>();

        for (FactorNode f : node.getNeighbors()) {
            double[] costs = new double[node.getVariable().getDomain().size()];
            Arrays.fill(costs, 0);
            costTable.put(f.getID(), costs);
        }

        noise = new  double[node.getVariable().getDomain().size();]
        for (int i = 0; i <noise.length; i++)
            noise[i] = Math.random();
    }

    public long getID() {
        return node.getID();
    }

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

    public double[] getNoise() {
        return noise;
    }

    public void sendMessages(int currCycle) {
        for (FactorNode fnode : node.getNeighbors()) {
            // todo: don't need to construct a new table all the times if you clone them later.
            double[] table = getCostTableSumExcluding(fnode.getID());
            // todo LATER: addUnaryConstraints(table);
            Commons.rmValue(table, Commons.getMin(table));
            Commons.addArray(table, getNoise());

            // Send messages to Funcation Nodes
            if (fnode.getOwner().equals(this)) {
                costTable.put(fnode.getID(), table.clone());
            } else {
                MaxSumAgent.VnodeToFnodeMessage msg =
                        new MaxSumAgent.VnodeToFnodeMessage(table, getID(), fnode.getID(), currCycle);
                fnode.getOwner().tell(msg, node.getOwner().getSelf());
            }
        }
    }

    public void copyCostTable(double[] table, long fNodeId) {
        // todo: don't need to clone the table here
        costTable.put(fNodeId, table.clone());
    }

    /**
     *
     * @param excludedId The ID of the function node which is excluded from summing the values of the cost table
     * @return The aggregated cost table
     * @// TODO: 5/25/17 If you need to search for more than one ID, then pass a HashSet.
     */
    public double[] getCostTableSumExcluding(long excludedId) {
        reading_cost_table = true;

        Domain dom = node.getVariable().getDomain();
        double[] sum = new double[dom.size()];
        for (int d = 0; d < dom.size(); d++) {
            sum[d] = 0;
            for (FactorNode f : node.getNeighbors()) {
                if (f.getID() == excludedId)
                    continue;
                sum[d] += costTable.get(f.getID())[d];
            }
        }

        reading_cost_table = false;
        return sum;
    }

    public double[] getCostTableSum() {
        return getCostTableSumExcluding(-1);
    }

    private void resetCostTable() {
        for (FactorNode f : node.getNeighbors())
            Arrays.fill(costTable.get(f), 0.0);
    }

}
