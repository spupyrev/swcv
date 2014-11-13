package edu.cloudy.metrics;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutResult;
import edu.cloudy.layout.WordGraph;

/**
 * May 3, 2013
 * computes aspect ration
 */
public class AspectRatioMetric implements QualityMetric
{
    //private static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5)) / 2.0;

    @Override
    public double getValue(WordGraph wordGraph, LayoutResult algo)
    {
        if (wordGraph.getWords().isEmpty())
            return 0;

        SWCRectangle bb = SpaceMetric.computeBoundingBox(wordGraph.getWords(), algo);
        double mn = Math.min(bb.getWidth(), bb.getHeight());
        double mx = Math.max(bb.getWidth(), bb.getHeight());
        double ratio = mx / mn;

        //double diff = Math.abs(ratio - GOLDEN_RATIO);
        return ratio;
    }
}
