package edu.cloudy.layout;

import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author spupyrev
 * Nov 11, 2014
 * 
 * Caches some statistics in the graph of words
 */
public class WordGraphCache
{
    private List<Word> words;
    private Map<WordPair, Double> similarity;
    private Map<WordPair, Double> distance;

    private Map<WordPair, Double> shortestPaths = new HashMap<WordPair, Double>();
    private Map<Word, Double> weightedDegree = new HashMap<Word, Double>();
    private Map<Word, Integer[]> nonZeroAdjacency = new HashMap<Word, Integer[]>();

    public WordGraphCache(List<Word> words, Map<WordPair, Double> similarity, Map<WordPair, Double> distance)
    {
        this.words = words;
        this.similarity = similarity;
        this.distance = distance;
    }

    public double shortestPath(Word w1, Word w2)
    {
        WordPair wp = new WordPair(w1, w2);
        if (!shortestPaths.containsKey(wp))
            initShortestPaths(w1);

        return shortestPaths.get(wp);
    }

    public double weightedDegree(Word w)
    {
        if (!weightedDegree.containsKey(w))
            initWeightedDegree(w);

        return weightedDegree.get(w);
    }

    public Integer[] nonZeroAdjacency(Word w)
    {
        if (!nonZeroAdjacency.containsKey(w))
            initNonZeroAdjacency(w);

        return nonZeroAdjacency.get(w);
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
                if (distance.containsKey(wp))
                {
                    double len = distance.get(wp);
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

    private void initWeightedDegree(Word s)
    {
        double wd = 0;
        for (int j = 0; j < words.size(); j++)
        {
            Word t = words.get(j);
            if (s.equals(t))
                continue;

            WordPair wp = new WordPair(s, t);
            wd += similarity.get(wp);
        }

        weightedDegree.put(s, wd);
    }

    private void initNonZeroAdjacency(Word s)
    {
        List<Integer> adj = new ArrayList();
        for (int i = 0; i < words.size(); i++)
        {
            Word t = words.get(i);
            if (s.equals(t))
                continue;

            WordPair wp = new WordPair(s, t);
            if (similarity.get(wp) > 0)
                adj.add(i);
        }

        nonZeroAdjacency.put(s, adj.toArray(new Integer[adj.size()]));
    }

}
