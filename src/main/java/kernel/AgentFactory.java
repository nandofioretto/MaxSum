package kernel;

import agent.MaxSum.MaxSumAgent;
import communication.ComAgent;
import communication.DCOPagent;

import java.util.List;

/**
 * Created by nandofioretto on 5/22/17.
 */
public class AgentFactory {

    public static DCOPagent create(ComAgent statsCollector, AgentState agtState, List<Object> algParameters) {
        // todo: Modify here to add possibly different algorithms.
        return new MaxSumAgent(statsCollector, agtState, algParameters);
    }

}
