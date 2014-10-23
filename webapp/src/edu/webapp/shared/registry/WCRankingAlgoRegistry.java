package edu.webapp.shared.registry;

import edu.webapp.shared.WCRankingAlgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class WCRankingAlgoRegistry
{
    private static List<WCRankingAlgo> algorithms = new ArrayList<WCRankingAlgo>();

    static
    {
        algorithms.add(new WCRankingAlgo("tf", "Term Frequency", false));
        algorithms.add(new WCRankingAlgo("tf-idf", "TF/ICF - BrownCorpus", true));
        algorithms.add(new WCRankingAlgo("lex", "Lexical Centrality", true));
    }

    public static List<WCRankingAlgo> list()
    {
        return algorithms;
    }

    public static WCRankingAlgo getDefault()
    {
        return getById("tf");
    }

    public static WCRankingAlgo getById(String id)
    {
        for (WCRankingAlgo algo : algorithms)
            if (algo.getId().equals(id))
                return algo;

        throw new RuntimeException("ranking algorithm with id '" + id + "' not found");
    }

    public static WCRankingAlgo getRandom()
    {
        return algorithms.get(new Random().nextInt(algorithms.size()));
    }
}
