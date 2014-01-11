package edu.cloudy.metrics;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cloudy.graph.Edge;
import edu.cloudy.graph.MaxSpanningTreeBuilder;
import edu.cloudy.graph.WordGraph;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class MaxPlanarSubgraphMetric implements QualityMetric {

	@Override
	public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo) {
		WordGraph wg = new WordGraph(words, similarity);
		Set<Edge> mstEdges = new HashSet<Edge>();
		WordGraph tree = new MaxSpanningTreeBuilder(wg).getTree(mstEdges);

		return Math.min(wg.totalWeight(), 3 * tree.totalWeight());
	}

}
