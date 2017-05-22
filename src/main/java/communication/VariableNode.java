package communication;

import agent.FactorGraphAgent;
import kernel.Variable;
import java.util.List;

/**
 * Created by nando on 5/17/17.
 */
public class VariableNode {
    private List<FunctionNode> neighbors;
    private DCOPagent owner;
    private Variable variable;

    public VariableNode(FactorGraphAgent owner, Variable variable) {
        this.owner = owner;
        this.variable = variable;
        owner.addVariableNode(this);
    }

    public void addNeighbor(FunctionNode fNode) {
        if (!neighbors.contains(fNode))
            neighbors.add(fNode);
    }

    public List<FunctionNode> getNeighbors() {
        return neighbors;
    }

    public DCOPagent getOwner() {
        return owner;
    }

    public Variable getVariable() {
        return variable;
    }
}
