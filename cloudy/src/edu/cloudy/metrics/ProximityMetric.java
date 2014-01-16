package edu.cloudy.metrics;

import edu.cloudy.layout.LayoutAlgo;
import edu.cloudy.nlp.Word;
import edu.cloudy.nlp.WordPair;
import edu.cloudy.utils.SWCRectangle;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mgaut
 */
public class ProximityMetric implements QualityMetric, AdjacentMetric
{
    //rectangles that are closer than EPS are considered as touching 
    private static double EPS = 0.01;

    @Override
    public double getValue(List<Word> words, Map<WordPair, Double> similarity, LayoutAlgo algo)
    {

        double res = 0;
        for (WordPair wp : similarity.keySet())
        {
            if (wp.getFirst().equals(wp.getSecond()))
                continue;

            if (close(algo, wp.getFirst(), wp.getSecond(), words))
            {
                res += similarity.get(wp);
            }
            else if (close(algo, wp.getSecond(), wp.getFirst(), words))
            {
                res += similarity.get(wp);
            }
        }

        return res;
    }

    private boolean close(LayoutAlgo algo, Word first, Word second, List<Word> words)
    {
        SWCRectangle rect1 = algo.getWordRectangle(first);
        SWCRectangle rect2 = algo.getWordRectangle(second);

        if (rect1 == null || rect2 == null)
            return false;

        if (touching(rect1, rect2))
            return true; // proximities should be a superset of adjacencies

        double c_x = rect1.getX() + rect1.getX() - rect1.getCenterX();
        double c_y = rect1.getY() + rect1.getY() - rect1.getCenterY();

        double e_width = rect1.getWidth() * 2;
        double e_height = rect1.getHeight() * 2;

        Ellipse2D elip = new Ellipse2D.Double(c_x, c_y, e_width, e_height);

        boolean inRange = elip.intersects(rect2.getX(), rect2.getY(), rect2.getWidth(), rect2.getHeight());

        // case: word 2 is not within the ellipse surrounding word 1
        if (!inRange)
            return false;

        // line connecting the two close words
        Line2D connection = new Line2D.Double(rect1.getCenterX(), rect1.getCenterY(), rect2.getCenterX(), rect2.getCenterY());

        // check for a word between our two "close" words
        // TODO: this could be more efficient, since only words that are close might have the possibility of intersection
        for (Word w : words)
        {

            // dont check collisions with the close words
            if (w.equals(first) || w.equals(second))
                continue;

            SWCRectangle tmpRect = algo.getWordRectangle(w);

            // see if our line intersects any of the words
            if (connection.intersects(tmpRect.getX(), tmpRect.getY(), tmpRect.getWidth(), tmpRect.getHeight()))
                return false;
        }

        return true;

    }

    public static boolean touching(SWCRectangle rect1, SWCRectangle rect2)
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

    public List<WordPair> getCloseWords(List<Word> words, LayoutAlgo algo)
    {
        List<WordPair> res = new ArrayList<WordPair>();
        for (int i = 0; i < words.size(); i++)
            for (int j = i + 1; j < words.size(); j++)
            {
                WordPair wp = new WordPair(words.get(i), words.get(j));

                if (close(algo, wp.getFirst(), wp.getSecond(), words))
                {
                    res.add(wp);
                }
                else if (close(algo, wp.getSecond(), wp.getFirst(), words))
                {
                    res.add(wp);
                }

            }

        return res;
    }

}
