package edu.cloudy.nlp.similarity;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author spupyrev
 * Oct 17, 2014
 */
public class SimilarityAlgorithmRegistry
{
    private static Map<String, Class<? extends SimilarityAlgo>> algorithms = new HashMap<String, Class<? extends SimilarityAlgo>>();

    static
    {
        algorithms.put("cos", CosineCoOccurenceAlgo.class);
        algorithms.put("jac", JaccardCoOccurenceAlgo.class);
        algorithms.put("lex", LexicalSimilarityAlgo.class);
    }

    public static SimilarityAlgo getById(String id)
    {
        try
        {
            Class<? extends SimilarityAlgo> cls = algorithms.get(id);
            Constructor<? extends SimilarityAlgo> ctor = cls.getConstructor();
            return ctor.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
