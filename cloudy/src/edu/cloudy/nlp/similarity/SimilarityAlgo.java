package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.SWCDocument;
import edu.cloudy.nlp.WordPair;

import java.util.Map;

public interface SimilarityAlgo
{
    public Map<WordPair, Double> computeSimilarity(SWCDocument wordifier);
}
