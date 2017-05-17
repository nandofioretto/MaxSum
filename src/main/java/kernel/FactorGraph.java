package kernel;

import communication.DCOPagent;
import communication.FunctionNode;
import communication.VariableNode;

import java.util.HashMap;
import java.util.List;

/**
 * Created by nando on 5/17/17.
 * Build a factor graph in a centralized fashion
 */
public class FactorGraph {

    private List<VariableNode> variableNodes;
    private List<FunctionNode> functionNodes;

    private HashMap<Variable, VariableNode> varToVariableNodeMap;
    private HashMap<Constraint, FunctionNode> conToFunctionNodeMap;

    FactorGraph(DCOPInstance DCOP) {
        varToVariableNodeMap = new HashMap<>();
        conToFunctionNodeMap = new HashMap<>();

        // Create variable nodes
        for (Variable v : DCOP.getDCOPVariable()) {
            long aId = v.getOwnerAgent().getID();
            DCOPagent agent = DCOPinfo.agentsRef.get(aId);
            VariableNode vnode = new VariableNode(agent, v);
            varToVariableNodeMap.put(v, vnode);
        }

        // Create function nodes
        for (Constraint c : DCOP.getDCOPConstraint()) {
            long aId = getOnwerId(c);
            DCOPagent agent = DCOPinfo.agentsRef.get(aId);
            FunctionNode fnode = new FunctionNode(agent, c);
            conToFunctionNodeMap.put(c, fnode);

            // Add function nodes neighbors (i.e., all variable nodes connected to it)
            // and add variable nodes neighbors (i.e., all function nodes whose scope contains this variable)
            for (Variable v : c.getScope()) {
                VariableNode vnode = varToVariableNodeMap.get(v);
                fnode.addNeighbor(vnode);
                vnode.addNeighbor(fnode);
            }
        }
    }

    public long getOnwerId(Constraint c) {
        long id = Integer.MAX_VALUE;
        for (Variable v : c.getScope())
            if (v.getOwnerAgent().getID() < id)
                id = v.getOwnerAgent().getID();
        return id;
    }

}
