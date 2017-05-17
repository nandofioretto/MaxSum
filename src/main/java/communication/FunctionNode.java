package communication;

import kernel.Constraint;

import java.util.List;

/**
 * Created by nando on 5/17/17.
 */
public class FunctionNode {
    private List<VariableNode> neighbors;
    private Constraint constraint;
    private DCOPagent owner;

    public FunctionNode(DCOPagent owner, Constraint constraint) {
        this.owner = owner;
        this.constraint = constraint;
    }

    public void addNeighbor(VariableNode varNode) {
        if (!neighbors.contains(varNode))
            neighbors.add(varNode);
    }

    public List<VariableNode> getNeighbors() {
        return neighbors;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public DCOPagent getOwner() {
        return owner;
    }
}
