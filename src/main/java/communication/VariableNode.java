package communication;

import agent.FactorGraphAgent;
import kernel.Variable;

import java.util.ArrayList;
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
        neighbors = new ArrayList<>();
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

    public long getID() {
        return variable.getID();
    }

    @Override
    public String toString() {
        String s = "VariableNode: " +
                " owner= " + owner.getName() +
                " variable= " + variable.getName() +
                " neighbors_con= ";
        for (FunctionNode f : neighbors)
            s += f.getConstraint().getName() + " ";
        return s;
    }
}
