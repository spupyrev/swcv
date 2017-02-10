package edu.cloudy.nlp.similarity;

import java.util.Map;

import edu.cloudy.nlp.ItemPair;
import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.Word;

public interface SimilarityAlgo
{
    public Map<ItemPair<Word>, Double> computeSimilarity(SWCDocument wordifier);
}
