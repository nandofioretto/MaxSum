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

