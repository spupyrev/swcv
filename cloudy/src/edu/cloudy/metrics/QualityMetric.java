package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;

/**
 * @author spupyrev
 * May 3, 2013
 */
public interface QualityMetric
{
    double getValue(WordGraph wordGraph, LayoutResult layout);
}
