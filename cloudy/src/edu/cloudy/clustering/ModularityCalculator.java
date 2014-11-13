package edu.cloudy.clustering;

import edu.cloudy.layout.WordGraph;
import edu.cloudy.nlp.Word;

/**
 * @author spupyrev
 * Jan 11, 2014
 */
public class ModularityCalculator
{
    public static double compute(Word[] words, double[][] similarities, int[] clusters, WordGraph sp)
    {
        double res = 0;

        double m = 0;
        for (int i = 0; i < words.length; i++)
            for (int j = i + 1; j < words.length; j++)
            {
                m += similarities[i][j];
            }

        if (m < 1e-4)
            return 0.0;

        for (int i = 0; i < words.length; i++)
            for (int j = 0; j < words.length; j++)
            {
                if (clusters[i] != clusters[j])
                    continue;

                double w = (i != j ? similarities[i][j] : 0);
                double deg_s = sp.weightedDegree(words[i]);
                double deg_t = sp.weightedDegree(words[j]);

                res += (w - deg_s * deg_t / (2.0 * m));
            }

        res /= (2.0 * m);

        return res;
    }
}
