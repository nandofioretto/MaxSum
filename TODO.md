# TODO List

[x] Import function for WCSPs 
[x] Test two agent problem neighbor exchange.
[x] Test three agents problem neighbor exchange.
[x] Test three agents (with one ternary constraint) neighbor exchange.
[x] Test factor graph information exchange.
[x] Test factor graph dependencies (neighbors of variables), (neighbors of functions)
[x] Test factor graph neighbors dependencies from agent prespective
[x] Implement logic for agents to start
[ ] Implement logic for variables nodes 
[ ] Implement logic for factor nodes
[ ] Add unary constraints, following Liel Example in selectBestValue (functionNode)
    [ ] Check unary constraints not mapped as FactorNodes
[ ] Check requirement of atomicity when reading from cost Table in var/fact Node. Here the risk is that another agent
    might modify the value of the table while I am reading it
[ ] You maight want to experiment with MAXSumASPVD [see VariableNode::selectBestValue()  (LielCode)]

