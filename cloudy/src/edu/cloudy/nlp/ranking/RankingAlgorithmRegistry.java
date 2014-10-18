package edu.cloudy.nlp.ranking;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author spupyrev
 * Oct 17, 2014
 */
public class RankingAlgorithmRegistry
{
    private static Map<String, Class<? extends RankingAlgo>> algorithms = new HashMap<String, Class<? extends RankingAlgo>>();

    static
    {
        algorithms.put("tf", TFRankingAlgo.class);
        algorithms.put("tf-idf", TFIDFRankingAlgo.class);
        algorithms.put("lex", LexRankingAlgo.class);
    }

    public static RankingAlgo getById(String id)
    {
        try
        {
            Class<? extends RankingAlgo> cls = algorithms.get(id);
            Constructor<? extends RankingAlgo> ctor = cls.getConstructor();
            return ctor.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
