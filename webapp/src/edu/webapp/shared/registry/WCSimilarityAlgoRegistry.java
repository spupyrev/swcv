package edu.webapp.shared.registry;

import edu.webapp.shared.WCSimilarityAlgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author spupyrev
 * Oct 11, 2014
 */
public class WCSimilarityAlgoRegistry
{
    private static List<WCSimilarityAlgo> algorithms = new ArrayList<WCSimilarityAlgo>();

    static
    {
        algorithms.add(new WCSimilarityAlgo("cos", "Cosine Coefficient"));
        algorithms.add(new WCSimilarityAlgo("jac", "Jaccard Coefficient"));
        algorithms.add(new WCSimilarityAlgo("lex", "Lexical Similarity"));
        algorithms.add(new WCSimilarityAlgo("euc", "Euclidean Distance"));
    }

    public static List<WCSimilarityAlgo> list()
    {
        return algorithms;
    }

    public static WCSimilarityAlgo getDefault()
    {
        return getById("cos");
    }

    public static WCSimilarityAlgo getById(String id)
    {
        for (WCSimilarityAlgo algo : algorithms)
            if (algo.getId().equals(id))
                return algo;

        throw new RuntimeException("similarity algorithm with id '" + id + "' not found");
    }

    public static WCSimilarityAlgo getRandom()
    {
        return algorithms.get(new Random().nextInt(algorithms.size()));
    }
}
