package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.ItemPair;

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
    private Map<ItemPair<Word>, Double> similarity;

    @Override
    protected void run(SWCDocument wordifier)
    {
        List<Word> words = wordifier.getWords();

        similarity = new HashMap<ItemPair<Word>, Double>();

        for (int i = 0; i < words.size(); i++)
        {
            Word x = words.get(i);
            Set<Integer> xSentences = new HashSet<Integer>(x.getSentences());

            for (int j = i + 1; j < words.size(); j++)
            {
                Word y = words.get(j);
                Set<Integer> ySentences = new HashSet<Integer>(y.getSentences());

                Set<Integer> sharedSentences = new HashSet<Integer>(xSentences);
                sharedSentences.retainAll(ySentences);

                Set<Integer> unionSentences = new HashSet<Integer>(xSentences);
                unionSentences.addAll(ySentences);

                // just count how many times those two occur in the same sentence
                double xySimilarity = ((double)sharedSentences.size()) / (double)(unionSentences.size());
                assert (0 <= xySimilarity && xySimilarity <= 1.0);

                similarity.put(new ItemPair<Word>(x, y), xySimilarity);
            }

            similarity.put(new ItemPair<Word>(x, x), 1.0);
        }

    }

    @Override
    protected Map<ItemPair<Word>, Double> getSimilarity()
    {
        return this.similarity;
    }

}
