package edu.cloudy.clustering;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author spupyrev
 * Jan 11, 2014
 */
public class ShortestPathCalculator
{
    private List<Word> words;
    private Map<WordPair, Double> distances;

    private Map<WordPair, Double> shortestPaths = new HashMap<WordPair, Double>();

    public ShortestPathCalculator(List<Word> words, Map<WordPair, Double> distances)
    {
        this.words = words;
        this.distances = distances;
    }

    public double compute(Word w1, Word w2)
    {
        WordPair wp = new WordPair(w1, w2);
        if (!shortestPaths.containsKey(wp))
            initShortestPaths(w1);

        return shortestPaths.get(wp);

    }

    private void initShortestPaths(Word s)
    {
        Map<Word, Integer> wIndex = new HashMap<Word, Integer>();
        for (int i = 0; i < words.size(); i++)
            wIndex.put(words.get(i), i);

        double INF = 123456789.0;

        double[] dist = new double[words.size()];
        Arrays.fill(dist, INF);
        dist[wIndex.get(s)] = 0;

        PriorityQueue<Word> q = new PriorityQueue<Word>();
        q.add(s);

        while (!q.isEmpty())
        {
            Word now = q.poll();

            int v = wIndex.get(now);
            for (int i = 0; i < words.size(); i++)
            {
                Word next = words.get(i);
                WordPair wp = new WordPair(now, next);
                if (distances.containsKey(wp))
                {
                    double len = distances.get(wp);
                    if (dist[i] > dist[v] + len)
                    {
                        dist[i] = dist[v] + len;
                        q.add(next);
                    }

                }
            }
        }

        for (int i = 0; i < words.size(); i++)
        {
            WordPair wp = new WordPair(s, words.get(i));
            shortestPaths.put(wp, dist[i]);
        }
    }
}
