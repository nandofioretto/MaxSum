package kernel;

import communication.DCOPagent;
import java.util.HashMap;

/**
 * Created by ffiorett on 8/3/15.
 */
public class DCOPinfo {
    public static int nbAgents;
    public static int nbConstraints;

    public static DCOPagent leaderAgent;
    public static HashMap<Long, DCOPagent> agentsRef = new HashMap<>();

    public static boolean isSAT;
}
