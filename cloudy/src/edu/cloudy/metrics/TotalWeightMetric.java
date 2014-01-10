package edu.cloudy.metrics;

import java.util.List;
import java.util.Map;

import edu.cloudy.graph.WordGraph;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class TotalWeightMetric implements QualityMetric {

	@Override
	public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		WordGraph wg = new WordGraph(words, similarity);
		return wg.totalWeight();
	}

}
