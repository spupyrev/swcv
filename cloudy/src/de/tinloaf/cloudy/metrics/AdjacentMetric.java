package de.tinloaf.cloudy.metrics;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.List;
import java.util.Map;

/**
 * @author mgaut72
 * May 3, 2013
 */
public interface AdjacentMetric {
	List<WordPair> getAdjacencies(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo);
}
