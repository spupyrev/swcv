package edu.cloudy.metrics;

import edu.cloudy.graph.Graph;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class TotalWeightMetric implements QualityMetric
{
    @Override
    public double getValue(WordGraph wordGraph, LayoutResult layout)
    {
        return new Graph(wordGraph).totalWeight();
    }

}
