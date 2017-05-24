package agent;

import communication.ComAgent;
import communication.DCOPagent;
import communication.FunctionNode;
import communication.VariableNode;
import kernel.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nando on 5/19/17.
 */
public abstract class FactorGraphAgent extends DCOPagent {

    // The list of function nodes and variable nodes owned by this agent
    private List<FunctionNode> functionNodes;
    private List<VariableNode> variableNodes;

    public FactorGraphAgent(ComAgent statsCollector, AgentState agentState) {

        super(statsCollector, agentState);
        variableNodes = new ArrayList<>();
        functionNodes = new ArrayList<>();
    }

    public void addFunctionNode(FunctionNode node) {
        if (!functionNodes.contains(node)) {
            functionNodes.add(node);
            System.out.println("Agent " + this.getName() + " registers function node " + node.toString());

        }
    }

    public void addVariableNode(VariableNode node) {
        if (!variableNodes.contains(node)) {
            variableNodes.add(node);
            System.out.println("Agent " + this.getName() + " registers variable node " + node.toString());
        }
    }

    public List<FunctionNode> getFunctionNodes() {
        return functionNodes;
    }

    public List<VariableNode> getVariableNodes() {
        return variableNodes;
    }
}
