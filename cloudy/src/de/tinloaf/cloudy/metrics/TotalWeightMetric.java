package de.tinloaf.cloudy.metrics;

import java.util.List;
import java.util.Map;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.graph.WordGraph;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.WordPair;

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
