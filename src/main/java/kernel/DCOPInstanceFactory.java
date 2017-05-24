/*
 * Copyright (c) 2015.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package kernel;


import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by ffiorett on 7/7/15
 * This is not a real Factory, but more an importer.
 * The term Factory was used for consistency
 */
public class DCOPInstanceFactory {

    private static final int XCSP_TYPE = 0;
    private static final int DZINC_TYPE = 1;
    private static final int USC_TYPE = 2;
    private static final int WCSP_TYPE = 3;

    public static DCOPInstance importDCOPInstance(String filename) {
        return importDCOPInstance(filename, -1);
    }

    public static DCOPInstance importDCOPInstance(String filename, int type) {
        String ext = FilenameUtils.getExtension(filename);
        if (ext.equalsIgnoreCase("xcsp") || ext.equalsIgnoreCase("xml") || type == XCSP_TYPE) {
            return createXCSPInstance(filename);
        } else if (ext.equalsIgnoreCase("usc") || type == USC_TYPE) {
            return createUSCInstance(filename);
        } else if (ext.equalsIgnoreCase("dzn") || type == DZINC_TYPE) {
            return createDZINCInstance(filename);
        } else if (ext.equalsIgnoreCase("dimacs") || ext.equalsIgnoreCase("wcsp") || type == WCSP_TYPE) {
            return createWCSPInstance(filename);
        }
        return null;
    }

    private static DCOPInstance createXCSPInstance(String filename) {

        DCOPInstance instance = new DCOPInstance();

        try {
            File fXmlFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            // Presentation
            NodeList presentation = doc.getElementsByTagName("presentation");
            String maximizeStr = presentation.item(0).getAttributes().getNamedItem("maximize").getNodeValue();
            Boolean maximize = Boolean.valueOf(maximizeStr);
            instance.setOptimization(maximize);

            int optType = maximize ? Constants.OPT_MAXIMIZE : Constants.OPT_MINIMIZE;

            // Agents
            NodeList agents = doc.getElementsByTagName("agent");
            for (int i = 0; i < agents.getLength(); i++) {
                Node nodeAgent = agents.item(i);
                String name = nodeAgent.getAttributes().getNamedItem("name").getNodeValue();

                // Create and store Agent in DCOP instance
                instance.addAgent(new AgentState(name, i));
            }

            // Variables
            NodeList variables = doc.getElementsByTagName("variable");
            for (int i = 0; i < variables.getLength(); i++) {
                Node nodeVariable = variables.item(i);
                String name = nodeVariable.getAttributes().getNamedItem("name").getNodeValue();
                String domainName = nodeVariable.getAttributes().getNamedItem("domain").getNodeValue();
                String agentName = nodeVariable.getAttributes().getNamedItem("agent").getNodeValue();

                // Get domain
                Node domainNode = getXMLNode(doc, "domain", domainName);
                String[] valuesStr = domainNode.getTextContent().split(Pattern.quote(".."));
                int min = Integer.valueOf(valuesStr[0]);
                int max = Integer.valueOf(valuesStr[1]);

                // Create and store Variable in DCOP instance
                AgentState agtOwner = instance.getAgent(agentName);
                Variable variable = VariableFactory.getVariable(name, min, max, "INT-BOUND", agtOwner);
                instance.addVariable(variable);
            }

            // Constraints
            NodeList constraints = doc.getElementsByTagName("constraint");
            for (int i = 0; i < constraints.getLength(); i++) {
                Node constraintNode = constraints.item(i);
                String name = constraintNode.getAttributes().getNamedItem("name").getNodeValue();
                int arity = Integer.valueOf(constraintNode.getAttributes().getNamedItem("arity").getNodeValue());
                String[] scopeStr = constraintNode.getAttributes().getNamedItem("scope").getTextContent().split(" ");
                String relName = constraintNode.getAttributes().getNamedItem("reference").getNodeValue();

                // Retrieve scope:
                ArrayList<Variable> scope = new ArrayList<Variable>();
                for (String s : scopeStr) {
                    scope.add(instance.getVariable(s));
                }

                // Get Relation
                Node relationNode = getXMLNode(doc, "relation", relName);
                String defCostStr = relationNode.getAttributes().getNamedItem("defaultCost").getNodeValue();
                int defaultValue = 0;
                if (defCostStr.equalsIgnoreCase("infinity"))
                    defaultValue = Constants.infinity;
                else if (defCostStr.equalsIgnoreCase("-infinity"))
                    defaultValue = -Constants.infinity;
                else
                    defaultValue = Integer.valueOf(defCostStr);
                String semantics = relationNode.getAttributes().getNamedItem("semantics").getNodeValue();

                // Create constraint
                Constraint constraint = ConstraintFactory.getConstraint(name, scope, defaultValue, semantics);

                // Add values
                int values[] = new int[arity];
                String[] valuesStr = relationNode.getTextContent().split(Pattern.quote("|"));

                for (String s : valuesStr) {
                    String costValue[] = s.split(Pattern.quote(":"));
                    String utilStr = costValue[0];
                    int utility =
                            utilStr.equalsIgnoreCase("infinity") ? Constants.infinity
                                    : utilStr.equalsIgnoreCase("-infinity") ? -Constants.infinity
                                    : Integer.valueOf(utilStr);
                    String tupleStr[] = costValue[1].split(Pattern.quote(" "));
                    assert (tupleStr.length == arity);
                    for (int t = 0; t < arity; t++) {
                        values[t] = Integer.valueOf(tupleStr[t]);
                    }
                    constraint.addValue(new Tuple(values), utility, optType);
                }

                // Store Constraint in DCOP instance
                instance.addConstraint(constraint);
            }

            return instance;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Line 1:
     *   <Problem name> <N> <K> <C> <UB>
     * where
     *   <N> is the number of variables (integer)
     *   <K> is the maximum domain size (integer)
     *   <C> is the total number of constraints (integer)
     *   <UB> is the global upper bound of the problem (long integer)
     * Variables:
     *   <domain size of variable with index 0> ...
     *   <domain size of variable with index N-1>
     * Constraints:
     *  <Arity of the constraint>
     *  <Index of the first variable in the scope of the constraint>
     *  ...
     *  <Index of the last variable in the scope of the constraint>
     *  <Default cost value>
     *  <Number of tuples with a cost different than the default cost>
     *  and for every tuple (again in one line):
     * @param filename
     * @return
     */
    private static DCOPInstance createWCSPInstance(String filename) {
        DCOPInstance instance = new DCOPInstance();
        int optType = Constants.OPT_MINIMIZE;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            // First line (problem preamble)
            String[] tokens = br.readLine().split(" ");
            String instance_name = tokens[0];
            int n = Integer.parseInt(tokens[1]);
            int k = Integer.parseInt(tokens[2]);
            int c = Integer.parseInt(tokens[3]);
            long ub = Integer.parseInt(tokens[4]);
            System.out.println("read: " + n + " " + " " + k + " " + c + " " + ub);

            // Second line (problem variables)
            tokens = br.readLine().split(" ");
            assert (tokens.length == n);
            for (int i = 0; i < n; i++) {
                String agtName = "agt_" + Integer.toString(i);
                String varName = "v_" + Integer.toString(i);

                // Create and store Agent in DCOP instance
                AgentState agt = new AgentState(agtName, i);
                instance.addAgent(agt);

                // Create Variable and it in the DCOP instance
                int min = 0;
                int max = Integer.parseInt(tokens[i]) - 1;
                Variable variable = VariableFactory.getVariable(varName, min, max, "INT-BOUND", agt);
                instance.addVariable(variable);
                System.out.println(variable.toString());
            }

            // Create Constraints
            int cIdx = 0;
            String line;
            while ((line = br.readLine()) != null) {
                tokens = line.split(" ");
                String name = "con_" + cIdx++;
                int j = 0;
                // Retrieve scope:
                ArrayList<Variable> scope = new ArrayList<Variable>();
                int arity = Integer.parseInt(tokens[0]);
                for (j = 1; j <= arity; j++) {
                    scope.add(instance.getVariable(Integer.parseInt(tokens[j])));
                }
                Integer defaultValue = Integer.parseInt(tokens[j++]);
                long numEntries = Integer.parseInt(tokens[j]);

                Constraint constraint = ConstraintFactory.getConstraint(name, scope, defaultValue, "soft");
                // Fill in constraint
                int values[] = new int[arity];
                for (int eid = 0; eid < numEntries; eid++)
                {
                    tokens = br.readLine().split(" ");
                    for (int i = 0; i < arity; i++)
                        values[i] = Integer.parseInt(tokens[i]);
                    double cost = Double.parseDouble(tokens[arity]);
                    constraint.addValue(new Tuple(values), cost, optType);
                }
                instance.addConstraint(constraint);
                System.out.println(constraint);
            }
            return instance;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
    /**
     * Get the XMLNode of the given categroy matching the given name.
     * @param doc The XML document.
     * @param tag The tag to match.
     * @param name The name to match.
     * @return
     */
    private static Node getXMLNode(Document doc, String tag, String name) {
        NodeList nlist = doc.getElementsByTagName(tag);
        for (int i = 0; i < nlist.getLength(); i++) {
            Node node = nlist.item(i);
            String nodeName = node.getAttributes().getNamedItem("name").getNodeValue();
            if (nodeName.equalsIgnoreCase(name)) {
                return node;
            }
        }
        return null;
    }

    private static DCOPInstance createUSCInstance(String filename) {
        return null;
    }

    private static DCOPInstance createDZINCInstance(String filename) {
        return null;
    }

}




