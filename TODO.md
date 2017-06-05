# MaxSum Actions:
- Give to each agent:
    1. One variable x (the one it controls)   [initNodes]
    2. The set of constraints whose scope S is s.t. x \in S is the 
       variable with smaller ID among all those in S
    3. Set A random assignment for the variable (0)
    4. At each cycle [onMailBoxEmpty]
       1. The variable node sums all messages received and chooses the lowest value
       2. If this node is a VariableNode:
          neighbors= *all* the constraints nodes this variable is connected to.
          For each n : neighbor
             T = sumMessages(n);
             addUnaryConstraints(T);
             subtractMinimumValue(T);
             addPreferences(T); [Adds the variable's dust (used as tie-breakers)]
             sendMessage(n, T);
       3. If this node is a FactorNode:
          neighbors= *all* variablesNodes in the scope of this factor node
          For each n : neighbor
             T = copy of constraint table involving var n
             if this agent controls n: (do nothing)
             else: T = T + msg_recv_from_n   <<< contains the values of the other vars for when its var is assigned 
             T_proj = getBestValues(T);          [projects the variable (n) where we are sending this message]
   		     subtractMinimumValue(T_proj);
		     sendMessage(n, T_proj);

# TODO List

[x] Import function for WCSPs 
[x] Test two agent problem neighbor exchange.
[x] Test three agents problem neighbor exchange.
[x] Test three agents (with one ternary constraint) neighbor exchange.
[x] Test factor graph information exchange.
[x] Test factor graph dependencies (neighbors of variables), (neighbors of functions)
[x] Test factor graph neighbors dependencies from agent prespective
[x] Implement logic for agents to start
[x] Prior sending a message, check if the node is controlled by the same agent.
[x] With the same logic, if this is the case, then do not expect to receive messages from that particular node.
[x] Implement logic for variables nodes 
[ ] Implement logic for factor nodes
[ ] Need a buffer for messages to ensure no cycle no. overlaps - use same tricks as
    done for the number of messages in the MaxSumAgent.java
    - In this case rather than processing the message (copying its table
     when you receive it, simply store it, and then process the messages
     all togheter when you have received them all from that cycle.
     
[ ] Check requirement of atomicity when reading from cost Table in var/fact Node. Here the risk is that another agent
    might modify the value of the table while I am reading it

[ ] Add unary constraints, following Liel Example in selectBestValue (functionNode)
    [ ] Check unary constraints not mapped as FactorNodes
[ ] You maight want to experiment with MAXSumASPVD [see VariableNode::selectBestValue()  (LielCode)]
[ ] Build a version of this solver which is compleatly synchronous? 
 - all agents operate in steps. When all agents finish a step the spowner sends a (re)start message to everyone,
   with new cycle number.