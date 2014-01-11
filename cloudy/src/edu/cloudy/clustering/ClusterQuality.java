package edu.cloudy.clustering;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author spupyrev
 * Jan 11, 2014
 */
public class ClusterQuality
{
    public static double compute(List<Word> words, Map<WordPair, Double> similarities, Map<Word, Integer> clusters)
    {
        double res = 0;

        double m = 0;
        for (int i = 0; i < words.size(); i++)
            for (int j = i + 1; j < words.size(); j++)
            {
                Word s = words.get(i);
                Word t = words.get(j);

                WordPair wp = new WordPair(s, t);
                if (!similarities.containsKey(wp))
                    continue;

                double w = similarities.get(wp);
                m += w;
            }

        Map<Word, Double> weightedDegree = new HashMap<Word, Double>();
        for (int i = 0; i < words.size(); i++)
        {
            Word s = words.get(i);
            double wd = 0;
            for (int j = 0; j < words.size(); j++)
            {
                if (i == j)
                    continue;

                Word t = words.get(j);

                WordPair wp = new WordPair(s, t);
                if (!similarities.containsKey(wp))
                    continue;

                wd += similarities.get(wp);
            }
            
            weightedDegree.put(s, wd);
        }

        Set<Integer> cl = new HashSet(clusters.values());
        for (int c : cl)
        {
            List<Word> group = KMeansPlusPlus.getGroup(clusters, c);
            for (int j = 0; j < group.size(); j++)
                for (int k = j + 1; k < group.size(); k++)
                {
                    Word s = group.get(j);
                    Word t = group.get(k);

                    WordPair wp = new WordPair(s, t);
                    if (!similarities.containsKey(wp))
                        continue;

                    double w = similarities.get(wp);
                    double deg_s = weightedDegree.get(s);
                    double deg_t = weightedDegree.get(t);

                    res += (w - deg_s * deg_t / (2.0 * m));
                }
        }

        res /= (2.0 * m);

        return res;
    }
}
