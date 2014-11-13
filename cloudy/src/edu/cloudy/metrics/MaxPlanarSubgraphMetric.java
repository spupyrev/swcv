package edu.cloudy.metrics;

import edu.cloudy.graph.Edge;
import edu.cloudy.graph.Graph;
import edu.cloudy.graph.MaxSpanningTreeBuilder;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class MaxPlanarSubgraphMetric implements QualityMetric
{
    @Override
    public double getValue(WordGraph wordGraph, LayoutResult layout)
    {
        Graph wg = new Graph(wordGraph);
        Set<Edge> mstEdges = new HashSet<Edge>();
        Graph tree = new MaxSpanningTreeBuilder(wg).getTree(mstEdges);

        return Math.min(wg.totalWeight(), 3 * tree.totalWeight());
    }

}
