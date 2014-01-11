package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.List;
import java.util.Map;

/**
 * @author mgaut72
 * May 3, 2013
 */
public interface AdjacentMetric {
	List<WordPair> getAdjacencies(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo);
}
