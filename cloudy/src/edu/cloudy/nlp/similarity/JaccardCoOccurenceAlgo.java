package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JaccardCoOccurenceAlgo implements SimilarityAlgo
{
    private Map<WordPair, Double> similarity;
    private WCVDocument wordifier;

    @Override
    public void initialize(WCVDocument wordifier)
    {
        this.wordifier = wordifier;
        this.similarity = null;
    }

    @Override
    public void run()
    {
        List<Word> words = wordifier.getWords();

        similarity = new HashMap<WordPair, Double>();

        for (Word x : words)
        {
            for (Word y : words)
            {
                if (x.stem.equals(y.stem))
                    continue;

                Set<Integer> sharedSentences = new HashSet<Integer>(x.getSentences());
                sharedSentences.retainAll(y.getSentences());

                Set<Integer> unionSentences = new HashSet<Integer>(x.getSentences());
                unionSentences.addAll(y.getSentences());

                // just count how many times those two occur in the same sentence
                WordPair xyPair = new WordPair(x, y);
                double xySimilarity = ((double)sharedSentences.size()) / (double)(unionSentences.size());
                assert (0 <= xySimilarity && xySimilarity <= 1.0);
                similarity.put(xyPair, xySimilarity);
            }
        }

    }

    @Override
    public Map<WordPair, Double> getSimilarity()
    {
        return this.similarity;
    }

}
