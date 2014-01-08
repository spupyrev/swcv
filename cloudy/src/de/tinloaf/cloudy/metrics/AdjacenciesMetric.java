package de.tinloaf.cloudy.metrics;

import de.tinloaf.cloudy.algos.LayoutAlgo;
import de.tinloaf.cloudy.text.Word;
import de.tinloaf.cloudy.utils.SWCRectangle;
import de.tinloaf.cloudy.utils.WordPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author spupyrev
 * May 3, 2013
 */
public class AdjacenciesMetric implements QualityMetric, AdjacentMetric
{
    //rectangles that are closer than EPS are considered as touching 
    private static double EPS = 0.01;

    @Override
    public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo)
    {
        SWCRectangle bb = SpaceMetric.computeBoundingBox(words, algo);

        double res = 0;
        for (WordPair wp : similarity.keySet())
        {
            if (wp.getFirst().equals(wp.getSecond()))
                continue;

            if (close(algo, wp.getFirst(), wp.getSecond()))
            {
                res += similarity.get(wp);
            }
        }

        return res;
    }

    private boolean close(LayoutAlgo algo, Word first, Word second)
    {
        SWCRectangle rect1 = algo.getWordRectangle(first);
        SWCRectangle rect2 = algo.getWordRectangle(second);

        return close(rect1, rect2);
    }

    public static boolean close(SWCRectangle rect1, SWCRectangle rect2)
    {
        //checking interections manually, since we want to use EPS
        if (rect1 == null || rect2 == null)
            return false;
        boolean xIntersect = intersect(Math.min(rect1.getWidth(), rect2.getWidth()), rect1.getMinX(), rect1.getMaxX(), rect2.getMinX(), rect2.getMaxX());
        boolean yIntersect = intersect(Math.min(rect1.getHeight(), rect2.getHeight()), rect1.getMinY(), rect1.getMaxY(), rect2.getMinY(), rect2.getMaxY());
        return xIntersect && yIntersect;
    }

    private static boolean intersect(double size, double m1, double M1, double m2, double M2)
    {
        if (M1 + size * EPS <= m2)
            return false;
        if (M2 + size * EPS <= m1)
            return false;

        return true;
    }

    public List<WordPair> getAdjacencies(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo)
    {
        List<WordPair> res = new ArrayList<WordPair>();
        for (WordPair wp : similarity.keySet())
        {
            if (wp.getFirst().equals(wp.getSecond()))
                continue;

            if (close(algo, wp.getFirst(), wp.getSecond()))
                res.add(wp);
        }

        return res;
    }

}
