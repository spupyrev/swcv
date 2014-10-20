package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.WordPair;

import java.util.Map;

/**
 * @author spupyrev
 * Oct 18, 2014
 */
public abstract class BaseSimilarityAlgo implements SimilarityAlgo
{

    @Override
    public Map<WordPair, Double> computeSimilarity(SWCDocument wordifier)
    {
        initialize(wordifier);
        run();
        return getSimilarity();
    }

    protected abstract void initialize(SWCDocument wordifier);

    protected abstract void run();

    protected abstract Map<WordPair, Double> getSimilarity();
}
