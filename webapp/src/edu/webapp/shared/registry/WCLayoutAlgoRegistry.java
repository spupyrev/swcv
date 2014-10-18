package edu.webapp.shared.registry;

import edu.webapp.shared.WCLayoutAlgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class WCLayoutAlgoRegistry
{
    private static final String ALGORITHM_TYPE_RANDOM = "Random";
    private static final String ALGORITHM_TYPE_TAGCLOUD = "Tag Cloud";
    private static final String ALGORITHM_TYPE_HEURISTIC = "Heuristic";
    private static final String ALGORITHM_TYPE_THEORETICAL = "Theoretical";

    private static List<WCLayoutAlgo> algorithms = new ArrayList<WCLayoutAlgo>();

    static
    {
        algorithms.add(new WCLayoutAlgo("rnd", "Wordle", ALGORITHM_TYPE_RANDOM));
        algorithms.add(new WCLayoutAlgo("tca", "sorted alphabetically", ALGORITHM_TYPE_TAGCLOUD));
        algorithms.add(new WCLayoutAlgo("tcr", "sorted by rank", ALGORITHM_TYPE_TAGCLOUD));
        algorithms.add(new WCLayoutAlgo("mds", "Force-Directed", ALGORITHM_TYPE_HEURISTIC));
        algorithms.add(new WCLayoutAlgo("cp", "Context Preserving", ALGORITHM_TYPE_HEURISTIC));
        algorithms.add(new WCLayoutAlgo("sc", "Seam Carving", ALGORITHM_TYPE_HEURISTIC));
        algorithms.add(new WCLayoutAlgo("ip", "Infalte and Push", ALGORITHM_TYPE_HEURISTIC));
        algorithms.add(new WCLayoutAlgo("sf", "Star Forest", ALGORITHM_TYPE_THEORETICAL));
        algorithms.add(new WCLayoutAlgo("cc", "Cycle Cover", ALGORITHM_TYPE_THEORETICAL));
    }

    public static List<WCLayoutAlgo> list()
    {
        return algorithms;
    }

    public static WCLayoutAlgo getDefault()
    {
        return getById("cp");
    }

    public static WCLayoutAlgo getById(String id)
    {
        for (WCLayoutAlgo algo : algorithms)
            if (algo.getId().equals(id))
                return algo;

        throw new RuntimeException("algorithm with id '" + id + "' not found");
    }

    public static WCLayoutAlgo getRandom()
    {
        return algorithms.get(new Random().nextInt(algorithms.size()));
    }
}
