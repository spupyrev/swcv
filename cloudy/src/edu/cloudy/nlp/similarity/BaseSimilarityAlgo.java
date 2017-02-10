package edu.cloudy.nlp.similarity;

import java.util.Map;

import edu.cloudy.nlp.ItemPair;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public abstract class BaseSimilarityAlgo implements SimilarityAlgo
{

    @Override
    public Map<ItemPair<Word>, Double> computeSimilarity(SWCDocument wordifier)
    {
        run(wordifier);
        return getSimilarity();
    }

    protected abstract void run(SWCDocument wordifier);

    protected abstract Map<ItemPair<Word>, Double> getSimilarity();
}
