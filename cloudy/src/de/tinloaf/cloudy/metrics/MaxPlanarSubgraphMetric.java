package de.tinloaf.cloudy.metrics;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.graph.Edge;
import de.tinloaf.cloudy.graph.MaxSpanningTreeBuilder;
import de.tinloaf.cloudy.graph.WordGraph;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.WordPair;

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
