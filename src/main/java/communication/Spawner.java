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

package communication;

import agent.MaxSum.MaxSumAgent;
import kernel.AgentState;
import kernel.DCOPInstance;
import kernel.DCOPinfo;

import java.util.*;

/**
 * Created by ffiorett on 7/10/15.
 * Spawns the problem agents and creates dependencies, according to their constraint graph.
 */
public class Spawner {

    private List<AgentState> spawnedAgentStates;
    private HashMap<String, DCOPagent> yellowPages;

    public Spawner(DCOPInstance instance) {
        spawnedAgentStates = new ArrayList<AgentState>();
        yellowPages = new HashMap<String, DCOPagent>();

        for (AgentState agt : instance.getDCOPAgents()) {
            spawnedAgentStates.add(agt);
        }

        DCOPinfo.nbAgents = spawnedAgentStates.size();
        DCOPinfo.nbConstraints = instance.getDCOPConstraint().size();
    }

    /**
     * Creates the AKKA Agent System, as well as a watcher. It spawns all DCOP agents, and it
     * creates their dependencies.
     */
    public void spawn(List<Object> algParameters) {

        // Spawn the Statistics collector Deamon
        final ComAgent statsCollector = new StatisticsDeamon(spawnedAgentStates.size());
        statsCollector.start();

        // Spawns agents and start the DCOP algorithm
        for (AgentState agtState : spawnedAgentStates) {
            final DCOPagent agt = DCOPagentFactory(algParameters, statsCollector, agtState);
            DCOPinfo.agentsRef.put(agt.getId(), agt);
            yellowPages.put(agtState.getName(), agt);
            assert agt != null;
            agt.start();
        }

        // Save leader AgentRef
        String leaderName = spawnedAgentStates.get(0).getName();
        DCOPinfo.leaderAgent = DCOPinfo.agentsRef.get(0);
        assert (DCOPinfo.leaderAgent.getId() == 0);

        // Links Agent Neighbors as ComAgent objects.
        for (AgentState agtState : this.spawnedAgentStates) {
            ComAgent actor = yellowPages.get(agtState.getName());
            for (AgentState neighbor : agtState.getNeighbors()) {
                DCOPagent neighborAgt = yellowPages.get(neighbor.getName());
                actor.tell(new Messages.RegisterNeighbor(neighborAgt, neighborAgt.getId()), ComAgent.noSender());
            }
            // Link Leader to each agent (used if needed by algorithm)
            DCOPagent agt = yellowPages.get(leaderName);
            actor.tell(new Messages.RegisterLeader(yellowPages.get(leaderName)), ComAgent.noSender());
        }

        // Wait some time for discovery phase
        try {Thread.sleep(DCOPinfo.nbAgents * 50);} catch (InterruptedException e) {e.printStackTrace();}

        // TODO: Add Factor graph information here, if we are going to create factor graph agents.

        // Signals start to all agents
        for (AgentState agtState : this.spawnedAgentStates) {
            DCOPagent actor = yellowPages.get(agtState.getName());
            actor.tell(new Messages.StartSignal(), ComAgent.noSender());
        }

        // System awaits termination
        try {
            for (DCOPagent agt : yellowPages.values()) {
                agt.join();
            }
            statsCollector.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Collection<DCOPagent> getSpawnedAgents() {
        return yellowPages.values();
    }

    private DCOPagent DCOPagentFactory(List<Object> algParameters, ComAgent statsCollector, AgentState agtState) {
        // todo: Modify here to add possibly different algorithms.
        return new MaxSumAgent(statsCollector, agtState, algParameters);
    }

}
