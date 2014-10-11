package edu.cloudy.metrics;

import edu.cloudy.graph.Edge;
import edu.cloudy.graph.MaxSpanningTreeBuilder;
import edu.cloudy.graph.WordGraph;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class MaxPlanarSubgraphMetric implements QualityMetric
{
    @Override
    public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutResult algo)
    {
        WordGraph wg = new WordGraph(words, similarity);
        Set<Edge> mstEdges = new HashSet<Edge>();
        WordGraph tree = new MaxSpanningTreeBuilder(wg).getTree(mstEdges);

        return Math.min(wg.totalWeight(), 3 * tree.totalWeight());
    }

}
