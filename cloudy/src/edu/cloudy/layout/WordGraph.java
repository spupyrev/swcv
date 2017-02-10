package edu.cloudy.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cloudy.nlp.ItemPair;
import edu.cloudy.nlp.Word;

/**
 * @author spupyrev
 * Jan 11, 2014
 * 
 * The object wraps words and similarities between them:
 *   - similarities are between [0..1]
 *   - word weights are between [1..5]
 */
public class WordGraph
{
    private List<Word> words;
    private Map<ItemPair<Word>, Double> similarity;
    private Map<ItemPair<Word>, Double> distance;

    private WordGraphCache cache;

    public WordGraph(List<Word> words, Map<ItemPair<Word>, Double> similarity)
    {
        this.words = words;
        this.similarity = similarity;

        checkConsistency();

        initializeDistances();
        cache = new WordGraphCache(words, similarity, distance);
    }

    public List<Word> getWords()
    {
        return words;
    }

    public Map<ItemPair<Word>, Double> getSimilarity()
    {
        return similarity;
    }

    public double distance(Word w1, Word w2)
    {
        return distance.get(new ItemPair<Word>(w1, w2));
    }

    public double weightedDegree(Word w)
    {
        return cache.weightedDegree(w);
    }

    public double shortestPath(Word w1, Word w2)
    {
        return cache.shortestPath(w1, w2);
    }

    public Integer[] nonZeroAdjacency(Word w)
    {
        return cache.nonZeroAdjacency(w);
    }

    public Word[] convertWordsToArray()
    {
        return words.toArray(new Word[words.size()]);
    }

    public double[][] convertSimilarityToArray()
    {
        double[][] result = new double[words.size()][words.size()];
        for (int i = 0; i < words.size(); i++)
            for (int j = 0; j < words.size(); j++)
            {
                ItemPair<Word> wp = new ItemPair<Word>(words.get(i), words.get(j));
                result[i][j] = similarity.get(wp);
            }

        return result;
    }

    private void initializeDistances()
    {
        distance = new HashMap<ItemPair<Word>, Double>();
        for (int i = 0; i < words.size(); i++)
            for (int j = 0; j < words.size(); j++)
            {
                ItemPair wp = new ItemPair<Word>(words.get(i), words.get(j));
                double sim = similarity.get(wp);
                double dist = LayoutUtils.idealDistanceConverter(sim);
                distance.put(wp, dist);
            }
    }

    void reorderWords(int startIndex)
    {
        int n = words.size();
        List<Word> path = new ArrayList<Word>();
        for (int i = 0; i < n; i++)
            path.add(words.get((i + startIndex + 1) % n));

        for (int i = 0; i < n; i++)
            words.set(i, path.get(i));
    }

    private void checkConsistency()
    {
        for (int i = 0; i < words.size(); i++)
        {
            Word wi = words.get(i);
            ItemPair<Word> wp = new ItemPair<Word>(wi, wi);

            assert (similarity.containsKey(wp) && similarity.get(wp) == 1.0);
            assert (1.0 <= wi.weight && wi.weight <= 5.0);

            for (int j = 0; j < words.size(); j++)
            {
                Word wj = words.get(j);
                ItemPair<Word> wp2 = new ItemPair<Word>(wi, wj);
                assert (similarity.containsKey(wp2));
                double sim = similarity.get(wp2);

                assert (0 <= sim && sim <= 1.0);
            }
        }
    }

}
