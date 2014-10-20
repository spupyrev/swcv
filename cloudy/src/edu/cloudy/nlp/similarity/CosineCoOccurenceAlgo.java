package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosineCoOccurenceAlgo extends BaseSimilarityAlgo
{
    private Map<WordPair, Double> similarity;
    private SWCDocument wordifier;

    private final static double SIMILARITY_THRESHOLD = 0.000001;

    @Override
    protected void initialize(SWCDocument wordifier)
    {
        this.wordifier = wordifier;
        this.similarity = null;
    }

    @Override
    protected void run()
    {
        List<Word> words = wordifier.getWords();
        Map<Word, Integer> wordToIndex = new HashMap<Word, Integer>();

        int coOccurrence[][] = new int[words.size()][words.size()];

        for (int i = 0; i < words.size(); i++)
        {
            wordToIndex.put(words.get(i), i);

            for (int j = i; j < words.size(); j++)
            {
                coOccurrence[i][j] = 0;
            }
        }

        for (Word x : words)
        {
            for (Word y : words)
            {
                List<Integer> sharedSentences = new ArrayList<Integer>(x.getSentences());

                sharedSentences.retainAll(y.getSentences());

                // count how many times those two occur in the same sentence
                int xIndex = wordToIndex.get(x);
                int yIndex = wordToIndex.get(y);

                coOccurrence[xIndex][yIndex] += sharedSentences.size();
            }
        }

        similarity = new HashMap<WordPair, Double>();
        // compute the similarity matrix

        boolean nonZero[] = new boolean[words.size()];

        for (int x = 0; x < words.size(); x++)
        {
            nonZero[x] = false;
        }

        for (int x = 0; x < words.size(); x++)
        {
            //System.out.println("X: " + x);
            for (int y = (x + 1); y < words.size(); y++)
            {
                double xy = 0;
                double x2 = 0;
                double y2 = 0;

                for (int z = 0; z < words.size(); z++)
                {
                    xy += coOccurrence[x][z] * coOccurrence[y][z];
                    x2 += coOccurrence[x][z] * coOccurrence[x][z];
                    y2 += coOccurrence[y][z] * coOccurrence[y][z];
                }

                WordPair xyPair = new WordPair(words.get(x), words.get(y));
                double xySimilarity;
                if (Math.abs(x2 * y2) < 1e-6)
                {
                    xySimilarity = 0.0;
                }
                else
                {
                    xySimilarity = xy / Math.sqrt(x2 * y2);
                }

                if (xySimilarity > CosineCoOccurenceAlgo.SIMILARITY_THRESHOLD)
                {
                    nonZero[x] = true;
                    nonZero[y] = true;

                    xySimilarity *= xySimilarity;
                }
                else
                {
                    xySimilarity = 0.0;
                }

                similarity.put(xyPair, xySimilarity);
            }
        }

        for (int x = 0; x < words.size(); x++)
        {
            WordPair pair = new WordPair(words.get(x), words.get(x));

            if (nonZero[x])
            {
                similarity.put(pair, 0.0);
            }
            else
            {
                //Logger.println("Got a zero vertex!");
                similarity.put(pair, 1.0);
            }
        }
    }

    @Override
    protected Map<WordPair, Double> getSimilarity()
    {
        return similarity;
    }

}
