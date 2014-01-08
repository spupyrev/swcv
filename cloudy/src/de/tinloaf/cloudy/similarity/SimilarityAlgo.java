package de.tinloaf.cloudy.similarity;

import de.tinloaf.cloudy.text.WCVDocument;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.Map;

public interface SimilarityAlgo {
	public void initialize(WCVDocument wordifier);
	public void run();
	
	public Map<WordPair, Double> getSimilarity();
}
