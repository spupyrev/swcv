package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Similarity between words based on their co-occurence in the same sentences 
 */
public class JaccardCoOccurenceAlgo extends BaseSimilarityAlgo
{
    private Map<WordPair, Double> similarity;

    @Override
    protected void run(SWCDocument wordifier)
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
    protected Map<WordPair, Double> getSimilarity()
    {
        return this.similarity;
    }

}
