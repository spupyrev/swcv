package edu.cloudy.nlp.similarity;

import edu.cloudy.nlp.WCVDocument;
import edu.cloudy.nlp.WordPair;

import java.util.Map;

public interface SimilarityAlgo {
	public void initialize(WCVDocument wordifier);
	public void run();
	
	public Map<WordPair, Double> getSimilarity();
}
