package edu.cloudy.metrics;

import edu.cloudy.geom.SWCRectangle;
import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;

import java.util.List;
import java.util.Map;

/**
 * May 3, 2013
 * computes aspect ration
 */
public class AspectRatioMetric implements QualityMetric
{
    //private static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5)) / 2.0;

    @Override
    public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo)
    {
        if (words.isEmpty())
            return 0;

        SWCRectangle bb = SpaceMetric.computeBoundingBox(words, algo);
        double mn = Math.min(bb.getWidth(), bb.getHeight());
        double mx = Math.max(bb.getWidth(), bb.getHeight());
        double ratio = mx / mn;

        //double diff = Math.abs(ratio - GOLDEN_RATIO);
        return ratio;
    }
}
